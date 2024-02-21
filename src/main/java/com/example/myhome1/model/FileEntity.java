package com.example.myhome1.model;

import javax.persistence.*;
import java.time.LocalDateTime; // Import LocalDateTime


@Entity
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "stored_filename")
    private String storedFilename;

    @Column(name = "is_latest")
    private Boolean isLatest;

    @Column(name = "is_newly_updated", nullable = false, columnDefinition = "boolean default false")
    private Boolean isNewlyUpdated = false;

    @Column(name = "version")
    private int version = 1;


    @Lob
    @Column(name = "file_content", columnDefinition = "CLOB")
    private String fileContent;

    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // Add referencedColumnName to specify the referenced column
    private User uploader;

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }
    public User getUploader() {
        return uploader;
    }


    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }



    // 기존 Getter 및 Setter 메서드

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public void setStoredFilename(String storedFilename) {
        this.storedFilename = storedFilename;
    }

    public Boolean isLatest() {
        return isLatest;
    }

    public void setIsLatest(Boolean isLatest) {
        this.isLatest = isLatest;
    }


    public Boolean isNewlyUpdated() {
        return isNewlyUpdated;
    }

    public void setNewlyUpdated(Boolean isNewlyUpdated) {
        this.isNewlyUpdated = isNewlyUpdated;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void increaseVersion() {
        this.version += 1;
    }
}
