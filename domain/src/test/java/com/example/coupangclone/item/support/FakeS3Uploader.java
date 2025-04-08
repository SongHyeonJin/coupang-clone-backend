package com.example.coupangclone.item.support;


import com.example.coupangclone.auth.S3UploadPort;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class FakeS3Uploader implements S3UploadPort {

    @Override
    public String upload(MultipartFile file) throws IOException {
        return "https://fake-s3-url.com/" + file.getOriginalFilename();
    }

}
