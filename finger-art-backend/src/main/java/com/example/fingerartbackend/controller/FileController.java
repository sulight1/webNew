package com.example.fingerartbackend.controller;

import com.example.fingerartbackend.common.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/files")
public class FileController {

    // 设置本地存储路径
    private static final String UPLOAD_PATH = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 确保目录存在
        File dir = new File(UPLOAD_PATH);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            System.out.println("创建目录: " + UPLOAD_PATH + " 结果: " + created);
        }

        // 生成文件名：时间戳 + 原始文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = System.currentTimeMillis() + "_" + (originalFilename != null ? originalFilename : "image.jpg");

        try {
            File dest = new File(UPLOAD_PATH + fileName);
            file.transferTo(dest);
            
            // 返回完整的网络访问路径，方便直接存储到数据库
            String fileUrl = "http://localhost:3000/uploads/" + fileName;
            return Result.success(fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("服务器内部错误: " + e.getMessage());
        }
    }
}
