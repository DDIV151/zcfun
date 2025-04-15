package com.ddiv.zcfun.service.impl;

import com.ddiv.zcfun.domain.FileUploadResult;
import com.ddiv.zcfun.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);
    @Value("${file.upload.path}")
    private String baseUrl;

    private static final long MAX_IMAGE_SIZE = 1024 * 1024 * 20;
    private static final long MAX_VIDEO_SIZE = 1024 * 1024 * 444;
    private static final String[] allowedTypes = {"image/jpeg", "image/png", "image/gif"};
    private static final String[] allowedVideoTypes = {"video/mp4", "video/quicktime"};

    // 目前只接受图片和视频
    @Override
    public boolean validateFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null)
            return false;
        if (contentType.startsWith("image/")) {
            return file.getSize() <= MAX_IMAGE_SIZE && Arrays.asList(allowedTypes).contains(contentType);
        } else if (file.getContentType().startsWith("video/")) {
            return file.getSize() <= MAX_VIDEO_SIZE && Arrays.asList(allowedVideoTypes).contains(contentType);
        }
        return false;
    }

    /**
     * 上传文件到指定目录，并返回上传结果。
     *
     * @param userId 用户ID，用于标识上传文件的用户（当前未使用，但保留以备将来扩展）。
     * @param file   要上传的文件，类型为MultipartFile，包含文件的原始名称、内容类型和大小等信息。
     * @return FileUploadResult 包含上传文件的原始名称、存储路径、内容类型和大小等信息的对象。如果上传失败，返回null。
     */
    @Override
    public FileUploadResult uploadFile(long userId, MultipartFile file) {
        try {
            // 创建存储目录，如果目录不存在则尝试创建
            File uploadDir = new File(baseUrl);
            if (!uploadDir.exists()) {
                if (!uploadDir.mkdirs())
                    throw new IOException("创建目录失败");
            }

            // 生成唯一文件名，避免文件名冲突
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID() + fileExtension;

            // 将文件保存到目标路径
            File targetFile = new File(uploadDir, uniqueFileName);
            file.transferTo(targetFile);

            // 构建并返回上传结果对象
            return new FileUploadResult(originalFilename, baseUrl + uniqueFileName, file.getContentType(), file.getSize());
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return null;
        }
    }

    @Override
    public File getFile(String filename) {
        return new File(baseUrl, filename);
    }

}
