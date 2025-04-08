package com.example.coupangclone.auth;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3UploadPort {
    String upload(MultipartFile file) throws IOException;
}
