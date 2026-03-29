package com.college.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/avatar")
public class AvatarController {

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        System.out.println("AvatarController.getAvatar called with filename: " + filename);
        try {
            // 构建头像文件路径
            String uploadDir = System.getProperty("user.dir") + "/uploads/avatars/";
            System.out.println("Upload directory: " + uploadDir);
            Path avatarPath = Paths.get(uploadDir, filename);
            System.out.println("Avatar path: " + avatarPath.toString());

            // 检查文件是否存在
            if (!Files.exists(avatarPath)) {
                // 如果指定的头像文件不存在，返回默认头像
                Path defaultAvatarPath = Paths.get(uploadDir + "default.jpg");
                if (!Files.exists(defaultAvatarPath)) {
                    // 如果连默认头像都不存在，返回空响应
                    return ResponseEntity.notFound().build();
                }
                avatarPath = defaultAvatarPath;
            }

            // 创建资源对象
            Resource resource = new FileSystemResource(avatarPath.toFile());

            // 确定文件的媒体类型
            String contentType = "application/octet-stream"; // 默认类型
            try {
                contentType = Files.probeContentType(avatarPath);
                if (contentType == null) {
                    // 如果无法确定内容类型，默认为图片JPEG
                    contentType = "image/jpeg";
                }
            } catch (IOException e) {
                contentType = "image/jpeg"; // 出错时默认为图片类型
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, contentType);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}