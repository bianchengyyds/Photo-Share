package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.TagCountDTO;
import com.photoshare.backend.dto.TagDTO;
import com.photoshare.backend.dto.TagViewDTO;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@Tag(name = "标签")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    @Operation(summary = "给相册或照片打标签（任何登录用户都可以添加）")
    public Result<TagViewDTO> addTag(@Valid @RequestBody TagDTO dto) {
        TagViewDTO tag = tagService.addTag(dto, SecurityUtils.getCurrentUserId());
        return Result.success(tag);
    }

    @GetMapping("/popular")
    @Operation(summary = "热门标签，按被使用次数排序，供搜索页筛选")
    public Result<List<TagCountDTO>> popularTags(@RequestParam(defaultValue = "30") int limit) {
        return Result.success(tagService.listPopularTags(limit));
    }

    @DeleteMapping("/{relationId}")
    @Operation(summary = "删除自己添加的标签")
    public Result<Void> deleteTag(@PathVariable String relationId) {
        tagService.deleteTag(relationId, SecurityUtils.getCurrentUserId());
        return Result.success();
    }
}
