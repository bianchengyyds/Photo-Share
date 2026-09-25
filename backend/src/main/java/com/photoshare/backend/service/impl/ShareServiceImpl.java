package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.AlbumCommentDTO;
import com.photoshare.backend.dto.ShareAlbumUpdateDTO;
import com.photoshare.backend.dto.ShareCommentDTO;
import com.photoshare.backend.dto.ShareLinkDTO;
import com.photoshare.backend.dto.ShareViewDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.ShareLink;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.mapper.ShareLinkMapper;
import com.photoshare.backend.service.AlbumCommentService;
import com.photoshare.backend.service.PhotoService;
import com.photoshare.backend.service.ShareService;
import com.photoshare.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ShareServiceImpl implements ShareService {

    private static final String PERM_GUEST = "GUEST";
    private static final String PERM_VIEWER = "VIEWER";
    private static final String PERM_MEMBER = "MEMBER";
    private static final String PERM_OWNER = "OWNER";

    @Autowired
    private ShareLinkMapper shareLinkMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private TagService tagService;

    @Autowired
    private AlbumCommentService albumCommentService;

    @Override
    public ShareLink createShareLink(ShareLinkDTO dto, String userId) {
        Album album = albumMapper.selectById(dto.getAlbumId());
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        if (userId == null || !userId.equals(album.getUserId())) {
            throw new RuntimeException("只能分享自己的相册");
        }

        ShareLink shareLink = new ShareLink();
        shareLink.setId(UUID.randomUUID().toString());
        shareLink.setAlbumId(dto.getAlbumId());
        shareLink.setShareCode(generateShareCode());
        shareLink.setPassword(dto.getPassword());
        shareLink.setExpireAt(dto.getExpireAt());
        shareLink.setAllowDownload(dto.getAllowDownload());
        // 未显式指定时默认允许协作者上传
        shareLink.setAllowUpload(dto.getAllowUpload() == null || dto.getAllowUpload());
        shareLink.setVisitCount(0);
        shareLink.setUserId(userId);
        shareLinkMapper.insert(shareLink);
        return shareLink;
    }

    @Override
    public ShareLink getShareByCode(String shareCode) {
        return shareLinkMapper.selectByShareCode(shareCode);
    }

    @Override
    public ShareViewDTO getShareView(String shareCode, String password, String currentUserId) {
        ShareLink shareLink = getLiveShareLink(shareCode);

        boolean requirePassword = shareLink.getPassword() != null;
        boolean verified = !requirePassword || shareLink.getPassword().equals(password);

        ShareViewDTO view = new ShareViewDTO();
        view.setShareCode(shareLink.getShareCode());
        view.setRequirePassword(requirePassword);
        view.setCanView(verified);
        view.setAllowDownload(shareLink.getAllowDownload());
        view.setExpireAt(shareLink.getExpireAt());
        view.setMyPermission(currentUserId == null ? PERM_GUEST : PERM_VIEWER);
        view.setCanContribute(false);
        view.setCanEdit(false);
        if (!verified) {
            // 密码未通过：只告知需要密码，不暴露相册标题与权限
            return view;
        }

        Album album = albumMapper.selectById(shareLink.getAlbumId());
        if (album != null) {
            view.setAlbumId(album.getId());
            view.setAlbumTitle(album.getTitle());
            view.setAlbumDescription(album.getDescription());
            view.setAlbumLikeCount(album.getLikeCount());
            view.setAlbumCommentCount(album.getCommentCount());
            view.setAlbumTags(tagService.listTags("ALBUM", album.getId()));
        }
        boolean owner = currentUserId != null && currentUserId.equals(shareLink.getUserId());
        boolean canEdit = canEdit(shareLink, currentUserId);
        view.setCanContribute(Boolean.TRUE.equals(shareLink.getAllowUpload()));
        view.setCanEdit(canEdit);
        if (owner) {
            view.setMyPermission(PERM_OWNER);
        } else if (canEdit) {
            view.setMyPermission(PERM_MEMBER);
        }
        return view;
    }

    @Override
    public List<Photo> getSharePhotos(String shareCode, String password, String currentUserId, String currentUserRole) {
        ShareLink shareLink = validateShareLink(shareCode, password);

        // 相册所有者走分享链接时，同样能看到自己待审核、被拒绝的照片
        if (currentUserId != null && currentUserId.equals(shareLink.getUserId())) {
            return photoService.listPhotos(shareLink.getAlbumId(), currentUserId, currentUserRole);
        }
        // 其余访客（游客与被授权的登录成员）只能看到已审核通过的照片
        return photoService.listPhotos(shareLink.getAlbumId(), null, "USER");
    }

    @Override
    public List<AlbumComment> getShareComments(String shareCode, String password) {
        ShareLink shareLink = validateShareLink(shareCode, password);
        return albumCommentService.listComments(shareLink.getAlbumId());
    }

    @Override
    public AlbumComment addShareComment(String shareCode, ShareCommentDTO dto, String currentUserId) {
        ShareLink shareLink = validateShareLink(shareCode, dto.getPassword());
        if (currentUserId == null) {
            throw new RuntimeException("登录后才能发表评论");
        }
        AlbumCommentDTO commentDTO = new AlbumCommentDTO();
        commentDTO.setContent(dto.getContent());
        commentDTO.setParentId(dto.getParentId());
        commentDTO.setMentionUserIds(dto.getMentionUserIds());
        return albumCommentService.addComment(shareLink.getAlbumId(), commentDTO, currentUserId);
    }

    @Override
    public List<Photo> uploadSharePhotos(String shareCode, String password, List<MultipartFile> files, String uploaderNickname) {
        ShareLink shareLink = validateShareLink(shareCode, password);

        if (!Boolean.TRUE.equals(shareLink.getAllowUpload())) {
            throw new RuntimeException("该分享链接未开启协作上传");
        }

        // 照片记在分享链接创建人（相册所有者）名下，并记录协作者昵称；
        // 一律进入待审核区，管理员审核通过后才转存到正式存储并出现在相册中
        return photoService.uploadContributorPhotos(
                shareLink.getAlbumId(), files, shareLink.getUserId(), uploaderNickname);
    }

    @Override
    public Album updateShareAlbum(String shareCode, String password, ShareAlbumUpdateDTO dto, String currentUserId) {
        ShareLink shareLink = validateShareLink(shareCode, password);
        assertCanEdit(shareLink, currentUserId);

        Album album = albumMapper.selectById(shareLink.getAlbumId());
        if (album == null) {
            throw new RuntimeException("相册不存在");
        }
        album.setTitle(dto.getTitle());
        album.setDescription(dto.getDescription());
        albumMapper.update(album);
        return album;
    }

    @Override
    public void deleteSharePhoto(String shareCode, String password, String photoId, String currentUserId) {
        ShareLink shareLink = validateShareLink(shareCode, password);
        assertCanEdit(shareLink, currentUserId);

        Photo photo = photoMapper.selectById(photoId);
        if (photo == null || !shareLink.getAlbumId().equals(photo.getAlbumId())) {
            throw new RuntimeException("该照片不在这个相册中");
        }
        // 软删除，与相册所有者在管理页删除的语义一致，可在回收站恢复
        photoMapper.softDeleteById(photoId);
    }

    @Override
    public byte[] downloadPhotosAsZip(String shareCode, String password) {
        ShareLink shareLink = validateShareLink(shareCode, password);

        if (!shareLink.getAllowDownload()) {
            throw new RuntimeException("该分享链接不允许下载");
        }

        List<Photo> photos = photoService.listPhotos(shareLink.getAlbumId(), null, "USER");
        if (photos.isEmpty()) {
            throw new RuntimeException("相册中没有照片");
        }

        Set<String> usedNames = new HashSet<>();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (Photo photo : photos) {
                byte[] fileBytes = downloadFile(photo.getFileUrl());
                String entryName = photo.getFileName();

                // 避免重名
                if (usedNames.contains(entryName)) {
                    entryName = photo.getId() + "_" + entryName;
                }
                usedNames.add(entryName);

                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(fileBytes);
                zos.closeEntry();
            }

            zos.finish();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("打包失败: " + e.getMessage());
        }
    }

    /**
     * 编辑权限由创建者在分享链接上分配：开启协作上传的链接，登录成员即可修改相册；
     * 相册所有者始终可编辑。游客无法获得编辑权限。
     */
    private boolean canEdit(ShareLink shareLink, String currentUserId) {
        if (currentUserId == null) {
            return false;
        }
        return currentUserId.equals(shareLink.getUserId())
                || Boolean.TRUE.equals(shareLink.getAllowUpload());
    }

    private void assertCanEdit(ShareLink shareLink, String currentUserId) {
        if (currentUserId == null) {
            throw new RuntimeException("请先登录后再修改相册");
        }
        if (!canEdit(shareLink, currentUserId)) {
            throw new RuntimeException("创建者未给登录成员开启修改权限");
        }
    }

    private ShareLink getLiveShareLink(String shareCode) {
        ShareLink shareLink = getShareByCode(shareCode);
        if (shareLink == null) {
            throw new RuntimeException("分享链接不存在");
        }
        if (shareLink.getExpireAt() != null && shareLink.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("分享链接已过期");
        }
        return shareLink;
    }

    private ShareLink validateShareLink(String shareCode, String password) {
        ShareLink shareLink = getLiveShareLink(shareCode);

        // 验证密码：游客与登录成员都要求，登录只用于解锁编辑权限
        if (shareLink.getPassword() != null && !shareLink.getPassword().equals(password)) {
            throw new RuntimeException("访问密码错误");
        }

        // 累加访问次数
        shareLink.setVisitCount(shareLink.getVisitCount() + 1);
        shareLinkMapper.updateVisitCount(shareLink);

        return shareLink;
    }

    private byte[] downloadFile(String fileUrl) throws Exception {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);

        try (InputStream is = conn.getInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } finally {
            conn.disconnect();
        }
    }

    private String generateShareCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
