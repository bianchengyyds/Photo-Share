package com.photoshare.backend.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// 待审核区存储：普通用户上传的照片先落到后端本地临时目录，等待管理员审核。
// 该服务始终使用本地磁盘，不受 storage.type（qiniu/local）配置影响。
// 审核通过后由 PhotoServiceImpl 把文件转存到正式存储（StorageService）并删除这里的临时文件。
@Service
public class PendingStorageService {

    @Value("${storage.local.base-path:./uploads}")
    private String basePath;

    private static final String PENDING_DIR = "pending";

    // 保存待审核文件，返回可直接访问的本地URL，例如 /uploads/pending/2026/09/20/xxx.jpg
    public String save(MultipartFile file) {
        try {
            return saveToDisk(file.getBytes(), getFileExtension(file.getOriginalFilename()));
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    private String saveToDisk(byte[] data, String extension) {
        try {
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String fileName = UUID.randomUUID().toString() + extension;
            String dir = basePath + "/" + PENDING_DIR + "/" + dateDir;
            File dirFile = new File(dir);
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            Files.write(new File(dir + "/" + fileName).toPath(), data);
            return "/uploads/" + PENDING_DIR + "/" + dateDir + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    // 判断某个URL是否为待审核区的本地文件
    public boolean isPendingUrl(String fileUrl) {
        return fileUrl != null && fileUrl.startsWith("/uploads/" + PENDING_DIR + "/");
    }

    // 把待审核URL解析成本地File对象
    public File resolve(String fileUrl) {
        String relative = fileUrl.replace("/uploads", "");
        return new File(basePath + relative);
    }

    public byte[] readBytes(String fileUrl) {
        try {
            return Files.readAllBytes(resolve(fileUrl).toPath());
        } catch (IOException e) {
            throw new RuntimeException("读取待审核文件失败: " + e.getMessage());
        }
    }

    public void delete(String fileUrl) {
        File file = resolve(fileUrl);
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
        return fileName.substring(fileName.lastIndexOf("."));
    }
}