package com.example.coupangclone.fake;

import com.example.coupangclone.config.S3Uploader;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FakeS3Uploader extends S3Uploader {
    public FakeS3Uploader() {
        super(null);
    }

    @Override
    public String upload(MultipartFile file) throws IOException {
        return "https://fake-s3-url.com/" + file.getOriginalFilename();
    }

}
