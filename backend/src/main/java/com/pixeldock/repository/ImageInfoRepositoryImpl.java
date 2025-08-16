package com.pixeldock.repository;

import com.pixeldock.entity.ImageInfo;
import org.springframework.stereotype.Repository;

@Repository
public class ImageInfoRepositoryImpl implements ImageInfoRepository {
    
    @Override
    public ImageInfo save(ImageInfo imageInfo) {
        // 临时实现，直接返回传入的对象，不进行数据库操作
        return imageInfo;
    }
}
