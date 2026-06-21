package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import com.example.fingerartbackend.config.AiImageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/files")
public class FileController {

    private static final String UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @Autowired
    private AiImageProperties aiImageProperties;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        File dir = new File(UPLOAD_PATH);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("创建目录: " + UPLOAD_PATH + " 结果: " + created);
        }

        String fileName = buildSafeFileName(file.getOriginalFilename());

        try {
            File dest = new File(UPLOAD_PATH + fileName);
            file.transferTo(dest);

            String base = aiImageProperties.getPublicBaseUrl();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            String fileUrl = base + "/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }

    /** 纯英文文件名，避免真机 URL 中文路径无法加载 */
    private static String buildSafeFileName(String originalFilename) {
        String ext = "jpg";
        if (originalFilename != null && originalFilename.contains(".")) {
            String raw = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (raw.matches("^(jpe?g|png|gif|webp|bmp)$")) {
                ext = raw.equals("jpeg") ? "jpg" : raw;
            }
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
    }
}
