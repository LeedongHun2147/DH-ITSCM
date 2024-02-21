package com.example.myhome1.controller;

import com.example.myhome1.model.FileEntity;
import com.example.myhome1.model.User;
import com.example.myhome1.repository.FileRepository;
import com.example.myhome1.repository.UserRepository;
import com.example.myhome1.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Controller
@RequestMapping("/file")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    private static final String UPLOAD_DIR = "uploads"; // 파일이 저장될 위치

    @Autowired
    private FileService fileService;
    @Autowired
    private FileRepository fileRepository;

    @GetMapping("/delete/{fileId}")
    public String deleteFileByGet(@PathVariable Long fileId) {

        return deleteFile(fileId);  // 기존의 POST로 작성한 메서드를 재활용
    }

    @PostMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long fileId) {
        try {
            // 파일 정보를 데이터베이스에서 조회
            FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
            if (fileEntity == null) {
                logger.warn("File with ID {} not found in database.", fileId);
                return "redirect:/file/list";
            }

            // 파일 시스템에서 파일 삭제
            Path filePath = Paths.get(UPLOAD_DIR, fileEntity.getStoredFilename());
            Files.deleteIfExists(filePath);

            // 데이터베이스에서 파일 정보 삭제
            fileRepository.delete(fileEntity);

            logger.info("File with ID {} deleted successfully.", fileId);

        } catch (Exception e) {
            logger.error("Error while deleting file with ID {}", fileId, e);
        }
        return "redirect:/file/list";
    }

    @GetMapping("/list")
    public String fileList(Model model) {
        List<FileEntity> files = fileRepository.findAll();
        model.addAttribute("files", files);
        return "board/file";  // board/file.html 템플릿으로 리턴
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            User uploader = fileService.getCurrentUser();
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String username = userDetails.getUsername();
            // 해당 파일명에 대한 최신 버전의 파일을 조회
            List<FileEntity> files = fileRepository.findAllByOriginalFilenameOrderByVersionDesc(originalFilename);

            // 기존 파일 중 가장 최신 버전을 가져옵니다.
            FileEntity latestFile = files.isEmpty() ? null : files.get(0);

            // 파일 이름 중복 방지를 위한 UUID 생성
            String uuid = UUID.randomUUID().toString();
            String storedFilename = uuid + "_" + originalFilename;

            // 디렉토리가 존재하지 않으면 생성
            Path dirPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);  // 디렉토리 생성
            }

            // 파일 시스템에 저장
            Path path = dirPath.resolve(storedFilename);
            Files.write(path, file.getBytes());

            FileEntity fileEntity = new FileEntity();
            fileEntity.setOriginalFilename(originalFilename);
            fileEntity.setStoredFilename(storedFilename);
            fileEntity.setUploadTime(LocalDateTime.now());
            fileEntity.setUploader(uploader); // uploader는 현재 로그인한 사용자 정보

            if (latestFile != null) {
                // 기존의 최신 버전 파일의 'isLatest' 상태를 false로 변경
                latestFile.setIsLatest(false);
                fileRepository.save(latestFile);  // 변경사항 저장

                // 새로운 파일의 버전을 설정 (이전 버전 + 1)
                fileEntity.setVersion(latestFile.getVersion() + 1);
            } else {
                fileEntity.setVersion(1);  // 첫 파일 업로드의 경우
            }
            fileEntity.setIsLatest(true);
            fileRepository.save(fileEntity);
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 처리 로직
        }
        return "redirect:/file/list";
    }



    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            FileEntity fileEntity = fileRepository.findById(fileId).orElse(null);
            if (fileEntity != null) {
                Path path = Paths.get(UPLOAD_DIR, fileEntity.getStoredFilename());
                Resource resource = new UrlResource(path.toUri());

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileEntity.getOriginalFilename() + "\"")
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 오류 처리 로직
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/versions/{fileId}")
    public String viewPreviousVersions(@PathVariable Long fileId, Model model) {
        FileEntity currentVersion = fileRepository.findById(fileId).orElse(null);
        FileEntity previousVersion = null;

        if (currentVersion != null) {
            previousVersion = fileRepository.findByOriginalFilenameAndVersion(currentVersion.getOriginalFilename(), currentVersion.getVersion() - 1).orElse(null);
        }
        logger.info("Fetched Previous Version: {}", previousVersion);

        // 파일 내용 읽기
        try {
            if(currentVersion != null) {
                Path currentPath = Paths.get(UPLOAD_DIR, currentVersion.getStoredFilename());
                String currentContent = new String(Files.readAllBytes(currentPath));
                model.addAttribute("currentContent", currentContent);
            }

            if(previousVersion != null) {
                Path previousPath = Paths.get(UPLOAD_DIR, previousVersion.getStoredFilename());
                String previousContent = new String(Files.readAllBytes(previousPath));
                model.addAttribute("previousContent", previousContent);
            }
        } catch (IOException e) {
            // 파일 읽기 중 오류 발생 시 처리 로직 (예: 로깅, 오류 메시지 설정 등)
            e.printStackTrace();
        }

        model.addAttribute("currentVersion", currentVersion);
        model.addAttribute("previousVersion", previousVersion);
        model.addAttribute("file", currentVersion); // file 객체 추가

        return "board/versions";  // 이 경로는 실제 Thymeleaf 템플릿의 경로로 변경해주세요.
    }

}
