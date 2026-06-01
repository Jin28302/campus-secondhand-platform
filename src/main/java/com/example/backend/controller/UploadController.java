package com.example.backend.controller;

import com.example.backend.common.R;
import com.example.backend.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

// 文件上传控制器 - 处理图片等文件上传
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    @Value("${file.upload-path}")
    private String uploadPath;

    // 上传文件 - 将文件保存到服务器本地目录，返回访问URL
    @PostMapping
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 提取原始文件扩展名
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf("."))
                : "";
        // 使用UUID重命名文件，防止文件名冲突
        String filename = UUID.randomUUID() + ext;

        // 创建目标目录（如不存在则创建）
        File dest = new File(uploadPath + filename);
        dest.getParentFile().mkdirs();
        try {
            // 将上传的文件写入目标路径
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }

        // 返回文件的相对访问路径
        return R.ok("/upload/" + filename);
    }
}
