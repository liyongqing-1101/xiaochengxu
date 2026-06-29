package com.wxjiaozi.service;

import java.io.InputStream;

/**
 * OSS文件存储服务接口
 */
public interface OssService {

    /**
     * 上传文件到OSS
     * @param inputStream 文件流
     * @param fileName   原始文件名
     * @param contentType 文件类型
     * @return OSS访问URL
     */
    String upload(InputStream inputStream, String fileName, String contentType);

    /**
     * 上传图片并添加水印
     * @param inputStream   图片流
     * @param fileName      文件名
     * @param contentType   文件类型
     * @param watermarkText 水印文字
     * @return OSS访问URL
     */
    String uploadWithWatermark(InputStream inputStream, String fileName, String contentType, String watermarkText);

    /**
     * 获取文件访问URL
     */
    String getFileUrl(String objectKey);

    /**
     * 删除OSS文件
     */
    void delete(String objectKey);

    /**
     * 下载OSS文件
     */
    byte[] download(String objectKey);
}
