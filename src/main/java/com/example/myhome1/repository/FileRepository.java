package com.example.myhome1.repository;

import com.example.myhome1.model.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    FileEntity findByOriginalFilename(String originalFilename);

    List<FileEntity> findAllByOriginalFilenameOrderByVersionDesc(String originalFilename);

    @Query("SELECT f FROM FileEntity f WHERE f.originalFilename = :filename AND f.version = :version")
    Optional<FileEntity> findByOriginalFilenameAndVersion(@Param("filename") String filename, @Param("version") int version);

    // 파일 내용을 DB에서 검색
    @Query("SELECT f.fileContent FROM FileEntity f WHERE f.id = :fileId")
    Optional<String> findFileContentById(@Param("fileId") Long fileId);

}
