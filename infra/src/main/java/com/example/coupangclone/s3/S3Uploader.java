package com.example.coupangclone.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.coupangclone.auth.S3UploadPort;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Uploader implements S3UploadPort {

    private final AmazonS3 amazonS3;

    private final S3Properties s3Properties;

    @Override
    public String upload(MultipartFile multipartFile) throws IOException {
        String originalFileName = multipartFile.getOriginalFilename();

        if (originalFileName == null) {
            throw new ErrorException(ExceptionEnum.IMAGE_FILENAME_NOT_FOUND);
        }

        if (!originalFileName.contains(".")) {
            throw new ErrorException(ExceptionEnum.IMAGE_EXTENSION_MISSING);
        }

        String fileExtension = getFileExtension(originalFileName);

        if (!isValidImageExtension(fileExtension)) {
            throw new IOException("Invalid file type. Only .jpg, .jpeg, and .png files are allowed.");
        }

        String fileName = generateFileName(originalFileName);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(multipartFile.getSize());
        metadata.setContentType(getContentType(fileExtension));

        amazonS3.putObject(s3Properties.getBucket(), fileName, multipartFile.getInputStream(), metadata);

        return amazonS3.getUrl(s3Properties.getBucket(), fileName).toString();
    }

    public boolean delete(String fileUrl) {
        try {
            String fileKey = extractFileKey(fileUrl);
            amazonS3.deleteObject(s3Properties.getBucket(), fileKey);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex > 0) ? fileName.substring(dotIndex + 1).toLowerCase() : "";
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png");
    }

    private String getContentType(String extension) {
        return switch (extension) {
            case "png" -> "image/png";
            case "jpeg" -> "image/jpeg";
            default -> "image/jpg";
        };
    }

    private String generateFileName(String originalName) {
        return UUID.randomUUID() + "_" + originalName;
    }

    private String extractFileKey(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}
