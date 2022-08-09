package com.lyd.config;

import com.aliyun.oss.OSSClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author 天狗
 * @desc: 将AliOss工具注册到bean
 * @date 2022/5/10
 */

@Data
@Configuration
public class AliOssConfig {
    // 地域节点
    @Value("${aliyun.oss.endpoint}")
    private String endpoint;
    @Value("${aliyun.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;
    @Value("${aliyun.oss.urlPrefix}")
    private String urlPrefix;

    @Value("${aliyun.oss.documentPath}")
    private static String documentPath;
    @Value("${aliyun.oss.thumbnailPath}")
    private static String thumbnailPath;
    @Value("${aliyun.oss.userHeadPath}")
    private static String userHeadPath;
    @Value("${aliyun.oss.videoPhotoPath}")
    private static String videoPhotoPath;



    @Bean
    public OSSClient ossClient() {
        endpoint = "https://"+endpoint;
        return new OSSClient(endpoint, accessKeyId, accessKeySecret);
    }

    @Bean
    public OssPath ossPath() {
        return new OssPath(documentPath,thumbnailPath,userHeadPath,videoPhotoPath);
    }

}
