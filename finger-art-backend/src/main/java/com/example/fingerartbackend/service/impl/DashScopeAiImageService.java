package com.example.fingerartbackend.service.impl;

import com.example.fingerartbackend.config.AiImageProperties;
import com.example.fingerartbackend.service.AiImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "ai.image.provider", havingValue = "dashscope", matchIfMissing = true)
public class DashScopeAiImageService implements AiImageService {

    private static final String UPLOAD_PATH =
            System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Autowired
    private AiImageProperties properties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String generateAndSave(String prompt) {
        AiImageProperties.DashScope cfg = properties.getDashscope();
        if (cfg.getApiKey() == null || cfg.getApiKey().isBlank()) {
            throw new RuntimeException(
                    "未配置通义万相 API Key。请在 application.properties 设置 ai.image.dashscope.api-key，" +
                            "或在 application-local.properties 中配置，或设置环境变量 DASHSCOPE_API_KEY。" +
                            "获取地址：https://bailian.console.aliyun.com/");
        }

        String taskId = createTask(prompt, cfg);
        String remoteUrl = pollTaskResult(taskId, cfg);
        return saveRemoteImage(remoteUrl);
    }

    private String createTask(String prompt, AiImageProperties.DashScope cfg) {
        String url = cfg.getBaseUrl() + "/api/v1/services/aigc/text2image/image-synthesis";

        Map<String, Object> body = new HashMap<>();
        body.put("model", cfg.getModel());
        body.put("input", Map.of("prompt", prompt));
        body.put("parameters", Map.of("size", "1024*1024", "n", 1));

        HttpHeaders headers = authHeaders(cfg);
        headers.set("X-DashScope-Async", "enable");

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("通义万相创建任务失败：" + response.getStatusCode());
        }

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.has("code") && !root.get("code").isNull()) {
                throw new RuntimeException("通义万相错误：" + root.path("message").asText("未知错误"));
            }
            String taskId = root.path("output").path("task_id").asText(null);
            if (taskId == null || taskId.isBlank()) {
                throw new RuntimeException("通义万相未返回 task_id");
            }
            return taskId;
        } catch (IOException e) {
            throw new RuntimeException("解析通义万相响应失败", e);
        }
    }

    private String pollTaskResult(String taskId, AiImageProperties.DashScope cfg) {
        String url = cfg.getBaseUrl() + "/api/v1/tasks/" + taskId;
        HttpHeaders headers = authHeaders(cfg);

        for (int i = 0; i < cfg.getMaxPollAttempts(); i++) {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("查询生图任务失败：" + response.getStatusCode());
            }

            try {
                JsonNode root = objectMapper.readTree(response.getBody());
                if (root.has("code") && !root.get("code").isNull()) {
                    throw new RuntimeException("通义万相错误：" + root.path("message").asText("未知错误"));
                }

                String status = root.path("output").path("task_status").asText("");
                if ("SUCCEEDED".equalsIgnoreCase(status)) {
                    JsonNode results = root.path("output").path("results");
                    if (results.isArray() && !results.isEmpty()) {
                        String imageUrl = results.get(0).path("url").asText(null);
                        if (imageUrl != null && !imageUrl.isBlank()) {
                            return imageUrl;
                        }
                    }
                    throw new RuntimeException("通义万相任务成功但未返回图片 URL");
                }
                if ("FAILED".equalsIgnoreCase(status) || "CANCELED".equalsIgnoreCase(status)) {
                    String msg = root.path("output").path("message").asText("生图任务失败");
                    throw new RuntimeException("通义万相：" + msg);
                }
            } catch (IOException e) {
                throw new RuntimeException("解析任务状态失败", e);
            }

            try {
                Thread.sleep(cfg.getPollIntervalMs());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("生图等待被中断");
            }
        }

        throw new RuntimeException("通义万相生图超时，请稍后重试");
    }

    private HttpHeaders authHeaders(AiImageProperties.DashScope cfg) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(cfg.getApiKey().trim());
        return headers;
    }

    private String saveRemoteImage(String remoteUrl) {
        byte[] bytes = downloadSignedUrl(remoteUrl);
        if (bytes == null || bytes.length == 0) {
            throw new RuntimeException("下载生成的图片失败");
        }

        File dir = new File(UPLOAD_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "ai_" + System.currentTimeMillis() + ".png";
        File dest = new File(UPLOAD_PATH + fileName);
        try {
            Files.write(dest.toPath(), bytes);
        } catch (IOException e) {
            throw new RuntimeException("保存图片失败", e);
        }

        String base = properties.getPublicBaseUrl();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/uploads/" + fileName;
    }

    /**
     * OSS 预签名 URL 不能被 RestTemplate 重新编码，否则 SignatureDoesNotMatch。
     */
    private byte[] downloadSignedUrl(String remoteUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(remoteUrl).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("下载通义万相图片失败（HTTP " + code + "）");
            }

            try (InputStream in = conn.getInputStream()) {
                return in.readAllBytes();
            }
        } catch (IOException e) {
            throw new RuntimeException("下载通义万相图片失败：" + e.getMessage(), e);
        }
    }
}
