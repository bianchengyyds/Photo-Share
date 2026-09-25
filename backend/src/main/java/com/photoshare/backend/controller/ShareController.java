package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.ShareAccessDTO;
import com.photoshare.backend.dto.ShareAlbumUpdateDTO;
import com.photoshare.backend.dto.ShareCommentDTO;
import com.photoshare.backend.dto.ShareLinkDTO;
import com.photoshare.backend.dto.ShareViewDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.AlbumComment;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.ShareLink;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.ShareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/shares")
@Tag(name = "分享链接")
public class ShareController {

    @Autowired
    private ShareService shareService;

    @PostMapping
    @Operation(summary = "创建分享链接")
    public Result<ShareLink> createShareLink(@Valid @RequestBody ShareLinkDTO dto) {
        String userId = SecurityUtils.getCurrentUserId();
        ShareLink shareLink = shareService.createShareLink(dto, userId);
        return Result.success(shareLink);
    }

    @GetMapping("/{code}")
    @Operation(summary = "获取分享详情（脱敏，含当前身份的权限）")
    public Result<ShareViewDTO> getShare(
            @PathVariable String code,
            @RequestParam(required = false) String password) {
        ShareViewDTO view = shareService.getShareView(code, password, SecurityUtils.getCurrentUserId());
        return Result.success(view);
    }

    @PostMapping("/{code}/photos")
    @Operation(summary = "获取分享照片列表（需验证密码）")
    public Result<List<Photo>> getSharePhotos(
            @PathVariable String code,
            @RequestBody(required = false) ShareAccessDTO dto) {
        String password = dto != null ? dto.getPassword() : null;
        List<Photo> photos = shareService.getSharePhotos(
                code, password, SecurityUtils.getCurrentUserId(), SecurityUtils.getCurrentUserRole());
        return Result.success(photos);
    }

    @PostMapping("/{code}/comments")
    @Operation(summary = "获取分享相册的评论列表（游客可读，需验证密码）")
    public Result<List<AlbumComment>> getShareComments(
            @PathVariable String code,
            @RequestBody(required = false) ShareAccessDTO dto) {
        String password = dto != null ? dto.getPassword() : null;
        return Result.success(shareService.getShareComments(code, password));
    }

    // 读评论已占用 POST /{code}/comments（密码走请求体而非 URL），创建入口只能另起一个路径
    @PostMapping("/{code}/comments/add")
    @Operation(summary = "在分享相册下发表评论或回复（链接即访问授权，但需登录）")
    public Result<AlbumComment> addShareComment(
            @PathVariable String code,
            @Valid @RequestBody ShareCommentDTO dto) {
        return Result.success(shareService.addShareComment(
                code, dto, SecurityUtils.getCurrentUserId()));
    }

    @PostMapping("/{code}/photos/upload")
    @Operation(summary = "协作者通过分享链接上传照片（需链接开启协作上传，上传后需管理员审核）")
    public Result<List<Photo>> uploadSharePhotos(
            @PathVariable String code,
            @RequestParam(required = false) String password,
            @RequestParam(value = "uploaderNickname", required = false) String uploaderNickname,
            @RequestParam("files") List<MultipartFile> files) {
        List<Photo> photos = shareService.uploadSharePhotos(code, password, files, uploaderNickname);
        return Result.success(photos);
    }

    @PutMapping("/{code}/album")
    @Operation(summary = "登录成员通过分享链接修改相册信息（需链接开启协作，仍需管理员审核照片）")
    public Result<Album> updateShareAlbum(
            @PathVariable String code,
            @Valid @RequestBody ShareAlbumUpdateDTO dto) {
        Album album = shareService.updateShareAlbum(code, dto.getPassword(), dto, SecurityUtils.getCurrentUserId());
        return Result.success(album);
    }

    @DeleteMapping("/{code}/photos/{photoId}")
    @Operation(summary = "登录成员通过分享链接删除相册内的照片（软删除）")
    public Result<Void> deleteSharePhoto(
            @PathVariable String code,
            @PathVariable String photoId,
            @RequestParam(required = false) String password) {
        shareService.deleteSharePhoto(code, password, photoId, SecurityUtils.getCurrentUserId());
        return Result.success();
    }

    @GetMapping("/{code}/download")
    @Operation(summary = "下载全部照片（ZIP）")
    public ResponseEntity<byte[]> downloadPhotos(
            @PathVariable String code,
            @RequestParam(required = false) String password) {
        byte[] zipBytes = shareService.downloadPhotosAsZip(code, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "photos.zip");
        headers.setContentLength(zipBytes.length);
        return ResponseEntity.ok().headers(headers).body(zipBytes);
    }
}
