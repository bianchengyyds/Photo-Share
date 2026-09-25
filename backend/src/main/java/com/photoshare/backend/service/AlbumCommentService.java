package com.photoshare.backend.service;

import com.photoshare.backend.dto.AlbumCommentDTO;
import com.photoshare.backend.entity.AlbumComment;

import java.util.List;

public interface AlbumCommentService {

    List<AlbumComment> listComments(String albumId);

    AlbumComment addComment(String albumId, AlbumCommentDTO dto, String userId);

    void deleteComment(String commentId, String userId);
}
