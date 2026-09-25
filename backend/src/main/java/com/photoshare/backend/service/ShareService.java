package com.photoshare.backend.service;

import com.photoshare.backend.dto.ShareAlbumUpdateDTO;
import com.photoshare.backend.dto.ShareCommentDTO;
import com.photoshare.backend.dto.ShareLinkDTO;
import com.photoshare.backend.dto.ShareViewDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.ShareLink;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ShareService {

    ShareLink createShareLink(ShareLinkDTO dto, String userId);

    ShareLink getShareByCode(String shareCode);

    // 分享页视图：按当前登录身份给出可执行的操作，不泄漏访问密码
    ShareViewDTO getShareView(String shareCode, String password, String currentUserId);

    List<Photo> getSharePhotos(String shareCode, String password, String currentUserId, String currentUserRole);

    // 分享页评论列表：游客也可见，因此与照片一样要先过链接密码校验
    List<AlbumComment> getShareComments(String shareCode, String password);

    // 分享页发表评论：链接本身就是访问授权，所以不再要求评论者是关注者，但必须登录
    AlbumComment addShareComment(String shareCode, ShareCommentDTO dto, String currentUserId);

    // 协作者通过分享链接上传照片（需链接开启 allowUpload），上传后进入待审核状态
    List<Photo> uploadSharePhotos(String shareCode, String password, List<MultipartFile> files, String uploaderNickname);

    // 被链接授权的登录成员修改相册信息
    Album updateShareAlbum(String shareCode, String password, ShareAlbumUpdateDTO dto, String currentUserId);

    // 被链接授权的登录成员删除相册内的照片（软删除）
    void deleteSharePhoto(String shareCode, String password, String photoId, String currentUserId);

    byte[] downloadPhotosAsZip(String shareCode, String password);
}
