package com.ddiv.zcfun.service;

import com.ddiv.zcfun.domain.FileUploadResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public interface FileService {
    boolean validateFile(MultipartFile file);

    FileUploadResult uploadFile(long userId, MultipartFile file);

    File getFile(String filename);

}
