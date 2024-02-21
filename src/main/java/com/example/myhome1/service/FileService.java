package com.example.myhome1.service;

import com.example.myhome1.model.FileEntity;
import com.example.myhome1.model.User; // Import your User model
import com.example.myhome1.repository.FileRepository;
import com.example.myhome1.repository.UserRepository; // Import your UserRepository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder; // Import the SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private UserRepository userRepository; // Autowire the UserRepository

    private static final String UPLOAD_DIR = "uploads"; // 파일이 저장될 위치

    public FileEntity handleFileUpload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        User uploader = getCurrentUser();

        // 해당 파일명에 대한 최신 버전의 파일을 조회
        List<FileEntity> files = fileRepository.findAllByOriginalFilenameOrderByVersionDesc(originalFilename);

        // 기존 파일 중 가장 최신 버전을 가져옵니다.
        FileEntity latestFile = files.isEmpty() ? null : files.get(0);

        FileEntity newFile = new FileEntity();
        newFile.setOriginalFilename(originalFilename);
        // Set the uploader for the new file
        newFile.setUploader(uploader);

        try {
            // 디렉토리가 존재하지 않으면 생성
            Path dirPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 파일을 서버의 파일 시스템에 저장
            String storedFilename = generateUniqueFilename(originalFilename);
            Path filePath = dirPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            newFile.setStoredFilename(storedFilename);

            if (latestFile != null) {
                // 기존의 최신 버전 파일의 'isLatest' 상태를 false로 변경
                latestFile.setIsLatest(false);
                fileRepository.save(latestFile);  // 변경사항 저장

                // 새로운 파일의 버전을 설정 (이전 버전 + 1)
                newFile.setVersion(latestFile.getVersion() + 1);
            }

            // 새로운 파일은 항상 최신 상태입니다.
            newFile.setIsLatest(true);
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 처리 로직
        }

        return fileRepository.save(newFile);  // 새로운 파일 정보를 DB에 저장
    }

    // 파일명 중복 방지를 위한 유니크한 파일명 생성
    private String generateUniqueFilename(String originalFilename) {
        // UUID를 활용한 파일명 생성
        String uuid = UUID.randomUUID().toString();
        return uuid + "_" + originalFilename;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 사용자 이름을 사용하여 사용자를 데이터베이스에서 검색
            return userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        }
        return null; // 사용자가 인증되지 않은 경우 또는 사용자를 찾을 수 없는 경우
    }


    // 기타 필요한 로직들...
}
