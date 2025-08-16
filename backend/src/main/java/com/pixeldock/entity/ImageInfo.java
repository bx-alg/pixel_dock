package com.pixeldock.entity;

import java.time.LocalDateTime;

// @Entity
// @Table(name = "image_info")
public class ImageInfo {
    
    // @Id
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // @Column(nullable = false)
    private String fileName;
    
    // @Column(nullable = false)
    private String originalFileName;
    
    // @Column(nullable = false)
    private String fileUrl;
    
    // @Column(nullable = false)
    private String filePath;
    
    // @Column(nullable = false)
    private Long fileSize;
    
    // @Column(nullable = false)
    private String contentType;
    
    // @Column(nullable = false)
    private LocalDateTime uploadTime;
    
    // @Column(nullable = false)
    private String uploadIp;
    
    // Constructors
    public ImageInfo() {}
    
    public ImageInfo(String fileName, String originalFileName, String fileUrl, String filePath, 
                    Long fileSize, String contentType, String uploadIp) {
        this.id = System.currentTimeMillis(); // 使用时间戳作为临时ID
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.uploadIp = uploadIp;
        this.uploadTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getOriginalFileName() {
        return originalFileName;
    }
    
    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    
    public String getFileUrl() {
        return fileUrl;
    }
    
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    
    public String getFilePath() {
        return filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    public Long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public LocalDateTime getUploadTime() {
        return uploadTime;
    }
    
    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }
    
    public String getUploadIp() {
        return uploadIp;
    }
    
    public void setUploadIp(String uploadIp) {
        this.uploadIp = uploadIp;
    }
}
