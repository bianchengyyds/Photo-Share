package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.service.SearchService;
import com.photoshare.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    private static final int MAX_PAGE_SIZE = 50;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Autowired
    private TagService tagService;

    @Override
    public List<Album> searchAlbums(ContentSearchDTO query, String viewerId) {
        List<Album> albums = albumMapper.searchAlbums(query, viewerId);
        String ownerId = query.getOwnerId();
        if (!ownerId.isEmpty()) {
            // 同一个作者的相册一次取回全部标签
            Map<String, List<TagViewDTO>> tagMap = tagService.mapTagsForOwnerAlbums(ownerId);
            for (Album album : albums) {
                album.setTags(tagMap.getOrDefault(album.getId(), List.of()));
            }
        } else {
            for (Album album : albums) {
                album.setTags(tagService.listTags("ALBUM", album.getId()));
            }
        }
        return albums;
    }

    @Override
    public List<Photo> searchPhotos(ContentSearchDTO query, String viewerId) {
        List<Photo> photos = photoMapper.selectVisiblePhotos(query, viewerId);
        tagService.attachPhotoTags(photos);
        return photos;
    }

    @Override
    public List<Album> listFollowedAlbums(String viewerId, int page, int size) {
        int limit = size < 1 || size > MAX_PAGE_SIZE ? 20 : size;
        int offset = (page < 1 ? 0 : page - 1) * limit;
        List<Album> albums = albumMapper.selectFollowingAlbums(viewerId, limit, offset);
        // 结果跨多个作者，用不上按单一归属人批量取标签的优化
        for (Album album : albums) {
            album.setTags(tagService.listTags("ALBUM", album.getId()));
        }
        return albums;
    }
}
