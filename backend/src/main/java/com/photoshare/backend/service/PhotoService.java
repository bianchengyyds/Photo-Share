package com.photoshare.backend.service;

import com.photoshare.backend.entity.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {

    List<Photo> uploadPhotos(String albumId, List<MultipartFile> files, String userId, String userRole);

    // 协作者通过分享链接上传：一律进入待审核区，并记录上传者昵称
    List<Photo> uploadContributorPhotos(String albumId, List<MultipartFile> files, String ownerId, String uploaderNickname);

    List<Photo> listPhotos(String albumId, String userId, String userRole);

    void deletePhoto(String id, String userId);

    List<Photo> listDeletedPhotos(String userId);

    void restorePhoto(String id, String userId);

    void permanentlyDeletePhoto(String id, String userId);

    List<Photo> listDeletedPhotosByAlbumId(String albumId);

    // 管理员审核相关方法
    List<Photo> listPendingPhotos();

    List<Photo> listAllPhotosForAdmin(String status);

    void approvePhoto(String photoId, String adminId);

    void rejectPhoto(String photoId, String adminId, String reason);

    void batchApprovePhotos(List<String> photoIds, String adminId);

    void batchRejectPhotos(List<String> photoIds, String adminId, String reason);
}
