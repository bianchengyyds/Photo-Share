package com.photoshare.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class LikeSummaryDTO {

    private List<String> likedAlbumIds;

    private List<String> likedPhotoIds;
}
