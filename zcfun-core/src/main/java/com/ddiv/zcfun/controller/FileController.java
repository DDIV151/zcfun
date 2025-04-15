package com.ddiv.zcfun.controller;


import com.ddiv.zcfun.domain.ApiResult;
import com.ddiv.zcfun.domain.FileUploadResult;
import com.ddiv.zcfun.domain.LoginUser;
import com.ddiv.zcfun.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RequestMapping("/files")
@Controller
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ApiResult uploadFile(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal LoginUser loginUser) {
        long userId = loginUser.getUser().getUserId();
        // 验证文件类型和大小
        if (!fileService.validateFile(file)) {
            return ApiResult.error(400, "文件类型或大小不符合要求");
        }
        FileUploadResult result = fileService.uploadFile(userId, file);
        if (result != null)
            return ApiResult.success(result);
        return ApiResult.error(500, "文件上传失败");
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            // 防止路径遍历攻击
            if (filename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }

            File file = fileService.getFile(filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(file.toURI());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(file.getName().substring(file.getName().lastIndexOf(".") + 1)))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }
}
