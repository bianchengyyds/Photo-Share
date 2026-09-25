package com.photoshare.backend.service;

import com.photoshare.backend.entity.Album;

public interface ContentAccessService {

    /** 本人和管理员一律可见；他人的相册必须设为 PUBLIC 且查看者已关注 */
    boolean canView(Album album, String viewerId, String viewerRole);

    /** 相册不存在或无权访问时抛业务异常，可访问时返回相册 */
    Album requireViewable(String albumId, String viewerId, String viewerRole);
}
