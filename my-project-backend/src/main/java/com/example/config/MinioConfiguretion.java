package com.example.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对象存储Minio
 */
@Configuration
public class MinioConfiguretion {
    @Value("${spring.minio.endpoint}")
    String endpoint;
    @Value("${spring.minio.username}")
    String username;
    @Value("${spring.minio.password}")
    String password;
    /**
     * 创建客户端Minio
     * @return
     */
    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(endpoint)  //对象存储服务地址，注意是9000那个端口
                .credentials(username, password)   //账户直接使用管理员
                .build();
    }
}
