package com.photoshare.backend.storage;

import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.storage.BucketManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
// @ConditionalOnProperty: Spring Boot 条件注解，根据配置属性决定是否加载 Bean
// 仅当storage.type为qiniu时才加载该服务
// 用于在application.yml中配置七牛云存储
@ConditionalOnProperty(name = "storage.type", havingValue = "qiniu")
public class QiniuStorageService implements StorageService {

    @Value("${storage.qiniu.access-key}")
    private String accessKey;

    @Value("${storage.qiniu.secret-key}")
    private String secretKey;

    @Value("${storage.qiniu.bucket}")
    private String bucket;

    @Value("${storage.qiniu.domain}")
    private String domain;

    
    // 获取七牛云上传管理器
    // 这个方法创建了一个"上传工具"，知道该往哪个机房传、怎么传。
    private UploadManager getUploadManager() {
        // Region.autoRegion():	自动选择最优上传区域
        Configuration cfg = new Configuration(Region.autoRegion());
        // UploadManager: 上传管理器，负责实际执行文件上传，提供 put() 方法来上传文件
        return new UploadManager(cfg);
    }

    private Auth getAuth() {
        return Auth.create(accessKey, secretKey);
    }

    @Override
    public String upload(MultipartFile file) {
        try {
            return doUpload(file.getBytes(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(byte[] data, String originalFileName) {
        return doUpload(data, originalFileName);
    }

    private String doUpload(byte[] data, String originalFileName) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = dateDir + "/" + UUID.randomUUID().toString() + getFileExtension(originalFileName);

            String token = getAuth().uploadToken(bucket);
            UploadManager uploadManager = getUploadManager();
            Response response = uploadManager.put(data, fileName, token);

            if (response.isOK()) {
                return domain + "/" + fileName;
            } else {
                throw new RuntimeException("七牛云上传失败: " + response.statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        try {
            // 从完整URL中提取文件key
            // replace: 将该字符串中的 参1子字符串 替换为 参2子字符串
            // String domain = "http://cdn.example.com";
            // String fileUrl = "http://cdn.example.com/2024/01/15/photo.jpg";
            // 结果为 "2024/01/15/photo.jpg"
            String key = fileUrl.replace(domain + "/", "");
            // BucketManager构造时已传入Auth，无需单独生成删除凭证
            BucketManager bucketManager = new BucketManager(getAuth(), new Configuration(Region.autoRegion()));
            Response response = bucketManager.delete(bucket, key);
            if (!response.isOK()) {
                throw new RuntimeException("七牛云删除失败: " + response.statusCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    // 获取文件扩展名
    // 例如: "image.jpg" -> ".jpg"
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        // substring 从index的位置开始截取，直到字符串末尾
        // lastIndexOf 返回最后一个指定字符或子字符串的index
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
