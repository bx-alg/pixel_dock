package com.pixeldock.repository;

import com.pixeldock.entity.ImageInfo;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;

// @Repository
public interface ImageInfoRepository {
    // extends JpaRepository<ImageInfo, Long> {
    // 可以根据需要添加自定义查询方法
    ImageInfo save(ImageInfo imageInfo);
}
