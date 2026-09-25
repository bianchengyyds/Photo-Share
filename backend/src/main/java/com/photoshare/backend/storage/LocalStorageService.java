package com.photoshare.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${storage.local.base-path:./uploads}")
    private String basePath;

    @Override
    public String upload(MultipartFile file) {
        try {
            return saveToDisk(file.getBytes(), getFileExtension(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public String upload(byte[] data, String originalFileName) {
        return saveToDisk(data, getFileExtension(originalFileName));
    }

    private String saveToDisk(byte[] data, String extension) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + extension;
            String dir = basePath + "/" + dateDir;
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            java.nio.file.Files.write(new File(dir + "/" + fileName).toPath(), data);
            return "/uploads/" + dateDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void delete(String fileUrl) {
        File file = new File(basePath + fileUrl.replace("/uploads", ""));
        if (file.exists()) {
            file.delete();
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
