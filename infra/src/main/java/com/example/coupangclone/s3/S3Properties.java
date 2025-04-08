package com.example.coupangclone.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cloud.aws")
public class S3Properties {
    private Credentials credentials;
    private Region region;
    private S3 s3;

    @Getter @Setter
    public static class Credentials {
        private String accessKey;
        private String secretKey;
    }

    @Getter @Setter
    public static class S3 {
        private String bucket;
    }

    @Getter @Setter
    public static class Region {
        private String staticRegion;
    }

    public String getBucket() {
        return s3.getBucket();
    }

}
