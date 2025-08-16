package com.pixeldock.service;

import com.pixeldock.entity.ImageInfo;
import com.pixeldock.repository.ImageInfoRepository;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ImageService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Autowired
    private ImageInfoRepository imageInfoRepository;
    
    @Value("${minio.bucket}")
    private String bucketName;
    
    @Value("${minio.endpoint}")
    private String minioEndpoint;
    
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    
    public ImageInfo uploadImage(MultipartFile file, String uploadIp) throws Exception {
        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过20MB");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只能上传图片文件");
        }
        
        // 生成唯一文件名
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // 构建文件路径
        String filePath = "uploads/" + LocalDateTime.now().getYear() + "/" + 
                         LocalDateTime.now().getMonthValue() + "/" + fileName;
        
        // 上传到MinIO
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filePath)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(contentType)
                    .build()
            );
        }
        
        // 构建访问URL
        String fileUrl = minioEndpoint + "/" + bucketName + "/" + filePath;
        
        // 保存到数据库（临时注释掉数据库操作）
        ImageInfo imageInfo = new ImageInfo(fileName, originalFileName, fileUrl, filePath, 
                                          file.getSize(), contentType, uploadIp);
        return imageInfoRepository.save(imageInfo);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
