package com.example.coupangclone.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader {
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + "_" + originalFileName;

        if (!isValidImageExtension(fileExtension)) {
            throw new IOException("Invalid file type. Only .jpg, .jpeg, and .png files are allowed.");
        }

        String contentType = "image/jpg";
        if (fileExtension.equalsIgnoreCase("png")) {
            contentType = "image/png";
        } else if (fileExtension.equalsIgnoreCase("jpeg")) {
            contentType = "image/jpeg";
        }

        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getSize());
        objMeta.setContentType(contentType);

        amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), objMeta);

        return amazonS3.getUrl(bucket, fileName).toString();
    }

    // 파일 삭제
    public boolean delete(String fileUrl) {
        try {
            String[] temp = fileUrl.split("/");
            String fileKey = temp[temp.length-1];
            amazonS3.deleteObject(bucket, fileKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg") || extension.equalsIgnoreCase("png");
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex > 0) ? fileName.substring(dotIndex + 1) : "";
    }

}
