package com.photoshare.backend.service.impl;

import com.photoshare.backend.dto.TagCountDTO;
import com.photoshare.backend.dto.TagDTO;
import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.entity.Tag;
import com.photoshare.backend.entity.TagRelation;
import com.photoshare.backend.mapper.AlbumMapper;
import com.photoshare.backend.mapper.PhotoMapper;
import com.photoshare.backend.mapper.TagMapper;
import com.photoshare.backend.mapper.TagRelationMapper;
import com.photoshare.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TagServiceImpl implements TagService {

    private static final String TARGET_ALBUM = "ALBUM";
    private static final String TARGET_PHOTO = "PHOTO";

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private TagRelationMapper tagRelationMapper;

    @Autowired
    private AlbumMapper albumMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Override
    public TagViewDTO addTag(TagDTO dto, String userId) {
        String type = normalizeType(dto.getTargetType());
        String targetId = dto.getTargetId();
        assertTargetExists(type, targetId);

        String name = dto.getName().trim();
        if (name.isEmpty()) {
            throw new RuntimeException("标签名不能为空");
        }

        Tag tag = tagMapper.selectByName(name);
        if (tag == null) {
            tag = new Tag();
            tag.setId(UUID.randomUUID().toString());
            tag.setName(name);
            tagMapper.insert(tag);
        }

        // 同一个人对同一个目标重复打同一个标签不会产生第二条记录
        TagRelation mine = tagRelationMapper.selectByTagAndTarget(tag.getId(), type, targetId, userId);
        if (mine != null) {
            return toView(mine, name);
        }
        TagRelation relation = new TagRelation();
        relation.setId(UUID.randomUUID().toString());
        relation.setTagId(tag.getId());
        relation.setUserId(userId);
        relation.setTargetType(type);
        relation.setTargetId(targetId);
        tagRelationMapper.insert(relation);
        return toView(relation, name);
    }

    @Override
    public void deleteTag(String relationId, String userId) {
        if (tagRelationMapper.deleteByIdAndUser(relationId, userId) == 0) {
            throw new RuntimeException("标签不存在或不是你自己添加的");
        }
    }

    @Override
    public List<TagCountDTO> listPopularTags(int limit) {
        return tagMapper.selectPopular(Math.max(1, Math.min(limit, 100)));
    }

    @Override
    public List<TagViewDTO> listTags(String targetType, String targetId) {
        return groupByTarget(tagRelationMapper.selectByTarget(normalizeType(targetType), targetId)).getOrDefault(targetId, new ArrayList<>());
    }

    @Override
    public Map<String, List<TagViewDTO>> mapTagsForAlbum(String albumId) {
        return groupByTarget(tagRelationMapper.selectTagsForAlbum(albumId));
    }

    @Override
    public Map<String, List<TagViewDTO>> mapTagsForOwnerAlbums(String userId) {
        return groupByTarget(tagRelationMapper.selectTagsForOwnerAlbums(userId));
    }

    @Override
    public void attachPhotoTags(List<Photo> photos) {
        Map<String, Map<String, List<TagViewDTO>>> byAlbum = new HashMap<>();
        for (Photo photo : photos) {
            Map<String, List<TagViewDTO>> tags = byAlbum.computeIfAbsent(
                    photo.getAlbumId(), this::mapTagsForAlbum);
            photo.setTags(tags.getOrDefault(photo.getId(), new ArrayList<>()));
        }
    }

    // 同名标签可能由不同的人各加一条，是否合并展示由前端按当前用户判断
    private Map<String, List<TagViewDTO>> groupByTarget(List<TagRelation> relations) {
        Map<String, List<TagViewDTO>> result = new LinkedHashMap<>();
        for (TagRelation relation : relations) {
            result.computeIfAbsent(relation.getTargetId(), k -> new ArrayList<>()).add(toView(relation, relation.getTagName()));
        }
        return result;
    }

    private TagViewDTO toView(TagRelation relation, String name) {
        TagViewDTO view = new TagViewDTO();
        view.setRelationId(relation.getId());
        view.setName(name);
        view.setUserId(relation.getUserId());
        return view;
    }

    private String normalizeType(String targetType) {
        String type = targetType == null ? "" : targetType.trim().toUpperCase();
        if (!TARGET_ALBUM.equals(type) && !TARGET_PHOTO.equals(type)) {
            throw new RuntimeException("标签目标类型只能是 ALBUM 或 PHOTO");
        }
        return type;
    }

    private void assertTargetExists(String type, String targetId) {
        if (TARGET_ALBUM.equals(type)) {
            if (albumMapper.selectById(targetId) == null) {
                throw new RuntimeException("相册不存在");
            }
        } else if (photoMapper.selectById(targetId) == null) {
            throw new RuntimeException("照片不存在");
        }
    }
}
