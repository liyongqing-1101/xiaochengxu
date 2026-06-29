package com.wxjiaozi.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.wxjiaozi.config.OssConfig;
import com.wxjiaozi.service.OssService;
import com.wxjiaozi.util.OssWatermarkUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OSS文件存储服务实现 (阿里云OSS)
 * [EXTENSION-POINT] 如需切换腾讯云COS, 实现此接口并替换@Service即可
 */
@Slf4j
@Service
public class OssServiceImpl implements OssService {

    private final OssConfig ossConfig;
    private OSS ossClient;

    public OssServiceImpl(OssConfig ossConfig) {
        this.ossConfig = ossConfig;
    }

    @PostConstruct
    public void init() {
        this.ossClient = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
        log.info("OSS客户端初始化完成, bucket={}", ossConfig.getBucketName());
    }

    @PreDestroy
    public void destroy() {
        if (ossClient != null) {
            ossClient.shutdown();
        }
    }

    @Override
    public String upload(InputStream inputStream, String fileName, String contentType) {
        String objectKey = generateObjectKey(fileName);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        PutObjectRequest request = new PutObjectRequest(
                ossConfig.getBucketName(), objectKey, inputStream, metadata);
        ossClient.putObject(request);
        log.info("文件上传成功: {}", objectKey);
        return getFileUrl(objectKey);
    }

    @Override
    public String uploadWithWatermark(InputStream inputStream, String fileName, String contentType, String watermarkText) {
        try {
            // 先读取全部字节以支持水印处理
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);
            byte[] originalBytes = buffer.toByteArray();

            // 添加水印
            byte[] watermarkedBytes = OssWatermarkUtil.addTextWatermark(
                    new ByteArrayInputStream(originalBytes), watermarkText);

            // 修改文件名为PNG
            String pngFileName = fileName.contains(".")
                    ? fileName.substring(0, fileName.lastIndexOf('.')) + ".png"
                    : fileName + ".png";

            return upload(new ByteArrayInputStream(watermarkedBytes), pngFileName, "image/png");
        } catch (Exception e) {
            log.error("水印上传失败, 尝试上传原图", e);
            // 水印失败时上传原图
            return upload(inputStream, fileName, contentType);
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        return String.format("https://%s.%s/%s",
                ossConfig.getBucketName(),
                ossConfig.getEndpoint(),
                objectKey);
    }

    @Override
    public void delete(String objectKey) {
        ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
        log.info("文件删除成功: {}", objectKey);
    }

    @Override
    public byte[] download(String objectKey) {
        try (InputStream is = ossClient.getObject(ossConfig.getBucketName(), objectKey).getObjectContent()) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectKey, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 生成OSS对象Key
     * 格式: exam/2024/01/01/uuid_filename
     */
    private String generateObjectKey(String originalFileName) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String uuid = IdUtil.fastSimpleUUID();
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        return String.format("exam/%s/%s_%s%s", datePath.replace("-", "/"), uuid, IdUtil.fastSimpleUUID().substring(0, 8), extension);
    }
}
