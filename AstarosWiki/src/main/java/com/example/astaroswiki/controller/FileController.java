package com.example.astaroswiki.controller;

import com.example.astaroswiki.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Tag(name = "文件管理", description = "图片上传接口")
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.base-url:/uploads}")
    private String baseUrl;

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10MB

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        if (file.getSize() > MAX_SIZE) {
            return Result.error("文件大小超过10MB限制");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            return Result.error("不支持的文件类型，仅允许 JPEG/PNG/GIF/WebP/SVG");
        }

        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;

            // Prevent directory traversal
            Path target = dirPath.resolve(fileName).normalize().toAbsolutePath();
            if (!target.startsWith(dirPath.normalize().toAbsolutePath())) {
                return Result.error("非法文件路径");
            }

            Files.copy(file.getInputStream(), target);
            String url = baseUrl + "/" + fileName;
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }
}
