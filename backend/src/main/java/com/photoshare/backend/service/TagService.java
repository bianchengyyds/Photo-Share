package com.photoshare.backend.service;

import com.photoshare.backend.dto.TagCountDTO;
import com.photoshare.backend.dto.TagDTO;
import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.entity.Photo;

import java.util.List;
import java.util.Map;

public interface TagService {

    TagViewDTO addTag(TagDTO dto, String userId);

    /** 搜索页的标签候选，按被使用次数倒序 */
    List<TagCountDTO> listPopularTags(int limit);

    void deleteTag(String relationId, String userId);

    List<TagViewDTO> listTags(String targetType, String targetId);

    // key 为相册ID或照片ID，一次取回相册本身与其内部照片的标签
    Map<String, List<TagViewDTO>> mapTagsForAlbum(String albumId);

    Map<String, List<TagViewDTO>> mapTagsForOwnerAlbums(String userId);

    /** 给一批照片就地填标签，跨相册时按相册聚合，一个相册一次查询 */
    void attachPhotoTags(List<Photo> photos);
}
