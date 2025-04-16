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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

/**
 * 文件控制器，用于处理文件上传和下载的请求。
 * 提供以下功能：
 * 1. 文件上传：支持用户上传文件，并验证文件类型和大小。
 * 2. 文件下载：根据文件名获取文件并返回文件资源。
 * <p>
 * 使用 `@RestController` 注解，表示该类是一个 RESTful 控制器，所有方法的返回值将直接作为 HTTP 响应体。
 * 使用 `@RequestMapping("/files")` 注解，表示该类的所有请求路径都以 `/files` 开头。
 */
@RequestMapping("/files")
@RestController
public class FileController {

    private final FileService fileService;

    /**
     * 构造函数，注入 `FileService` 依赖。
     *
     * @param fileService 文件服务，用于处理文件相关的业务逻辑。
     */
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 处理文件上传请求。
     *
     * @param file      上传的文件，类型为 `MultipartFile`。
     * @param loginUser 当前登录用户，通过 `@AuthenticationPrincipal` 注解注入。
     * @return ApiResult 包含上传结果的对象。如果上传成功，返回文件信息；如果失败，返回错误信息。
     */
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

    /**
     * 根据文件名获取文件并返回文件资源。
     *
     * @param filename 文件名，包含文件扩展名（如 "example.jpg"）。
     * @return ResponseEntity<Resource> 包含文件资源的响应实体。如果文件不存在或发生错误，返回相应的 HTTP 状态码。
     * @throws Exception 如果文件读取或处理过程中发生异常。
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        try {
            // 防止路径遍历攻击
            if (filename.contains("..")) {
                return ResponseEntity.badRequest().build(); // 返回 400 Bad Request
            }

            // 获取文件对象
            File file = fileService.getFile(filename);
            if (!file.exists()) {
                return ResponseEntity.notFound().build(); // 返回 404 Not Found
            }

            // 将文件转换为 Resource 对象
            Resource resource = new UrlResource(file.toURI());

            // 获取文件的 MIME 类型
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "application/octet-stream"; // 默认 MIME 类型
            }

            // 返回文件资源
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType)) // 设置 Content-Type
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + filename + "\"") // 设置 Content-Disposition
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build(); // 返回 500 Internal Server Error
        }
    }
}

