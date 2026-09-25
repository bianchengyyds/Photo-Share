package com.photoshare.backend.controller;

import com.photoshare.backend.common.Result;
import com.photoshare.backend.dto.ContentSearchDTO;
import com.photoshare.backend.entity.Album;
import com.photoshare.backend.entity.Photo;
import com.photoshare.backend.security.SecurityUtils;
import com.photoshare.backend.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@Tag(name = "搜索与动态")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping("/albums")
    @Operation(summary = "按标题、描述、标签搜索相册，可用时间与归属人筛选")
    public Result<List<Album>> albums(@ModelAttribute ContentSearchDTO query) {
        return Result.success(searchService.searchAlbums(query, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/photos")
    @Operation(summary = "搜索照片，可按相册、标签、时间筛选")
    public Result<List<Photo>> photos(@ModelAttribute ContentSearchDTO query) {
        return Result.success(searchService.searchPhotos(query, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/following-albums")
    @Operation(summary = "动态：我已关注用户公开出来的相册")
    public Result<List<Album>> followingAlbums(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(searchService.listFollowedAlbums(
                SecurityUtils.getCurrentUserId(), page, size));
    }
}
