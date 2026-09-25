package com.photoshare.backend.storage;

import org.springframework.web.multipart.MultipartFile;

// LocalStorageService（本地存储）和QiniuStorageService（七牛云）都实现了该接口
// 该接口代表"正式存储"：照片通过管理员审核后，最终会落到这里
public interface StorageService {

    String upload(MultipartFile file);

    // 审核通过后，把待审核区（本地临时文件）的字节流转存到正式存储
    String upload(byte[] data, String originalFileName);

    void delete(String fileUrl);
}