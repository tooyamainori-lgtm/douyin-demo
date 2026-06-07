package com.douyin.util;

import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * MinIO 工具类 — 文件上传 / 下载链接 / 删除
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.public-endpoint:#{null}}")
    private String publicEndpoint;

    /**
     * 获取浏览器可访问的 MinIO 基础 URL
     * Docker 部署时 public-endpoint 设为 http://localhost:9000，本地开发时为空则回退到 endpoint
     */
    private String getBaseUrl() {
        return (publicEndpoint != null && !publicEndpoint.isEmpty()) ? publicEndpoint : endpoint;
    }

    /**
     * 上传视频文件到 MinIO
     *
     * @param file 视频文件
     * @return 对象名称（如 videos/uuid.mp4）
     */
    public String uploadVideo(MultipartFile file) throws Exception {
        String originalName = file.getOriginalFilename();
        String fileExt = ".mp4";
        if (originalName != null && originalName.contains(".")) {
            fileExt = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = "videos/" + UUID.randomUUID().toString().replace("-", "") + fileExt;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "video/mp4")
                            .build()
            );
            log.info("视频已上传到 MinIO：{}", objectName);
        }

        return objectName;
    }

    /**
     * 上传本地文件到 MinIO
     *
     * @param localPath  本地文件路径
     * @param objectName MinIO 对象名称
     * @param contentType MIME 类型
     */
    public void uploadFile(String localPath, String objectName, String contentType) throws Exception {
        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .filename(localPath)
                        .contentType(contentType != null ? contentType : "application/octet-stream")
                        .build()
        );
        log.info("文件已上传到 MinIO：{}", objectName);
    }

    /**
     * 上传图片文件到 MinIO
     *
     * @param file       图片文件
     * @param subDir     子目录（如 avatars、covers）
     * @return 对象名称
     */
    public String uploadImage(MultipartFile file, String subDir) throws Exception {
        String originalName = file.getOriginalFilename();
        String fileExt = ".png";
        if (originalName != null && originalName.contains(".")) {
            fileExt = originalName.substring(originalName.lastIndexOf("."));
        }
        String objectName = subDir + "/" + UUID.randomUUID().toString().replace("-", "") + fileExt;

        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("图片已上传到 MinIO：{}", objectName);
        }

        return objectName;
    }

    /**
     * 获取 MinIO 文件的公开访问 URL
     *
     * @param objectName 对象名称
     * @return 完整访问 URL
     */
    public String getPublicUrl(String objectName) {
        return getBaseUrl() + "/" + bucket + "/" + objectName;
    }

    /**
     * 删除 MinIO 文件
     *
     * @param objectName 对象名称
     */
    public void delete(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .build()
            );
            log.info("MinIO 文件已删除：{}", objectName);
        } catch (Exception e) {
            log.error("MinIO 文件删除失败：{}", objectName, e);
        }
    }
}
