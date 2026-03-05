package com.example.chattingroom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    // 파일을 저장할 로컬 폴더 경로 설정 -> 실제 메신저의 파일전송은 FTP가 아닌 클라우드 서버에 이미지를 HTTP로 전송하고, URL에 그 주소를 보내서 받는사람쪽에서 그 이미지를 불러오게끔.
    private final String uploadDir = System.getProperty("user.dir") + "/uploads/";

    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file){
        try{
            File dir = new File(uploadDir);
            if(!dir.exists())
                dir.mkdirs();

            // 이미지 원본 앞에 UUID를 붙여 충돌을 방지. A,B가 cat.png를 보내면 서버에서 덮어쓰기되어버리므로.
            String originalName = file.getOriginalFilename();
            String uniqueName = UUID.randomUUID().toString() + "_" + originalName;

            // 서버의 HDD에 물리적으로 저장
            File destination = new File(uploadDir + uniqueName);
            file.transferTo(destination);

            // 프론트엔드가 이 이미지를 띄울 수 있도록 가상의 접속 주소 (URL) 을 영수증처럼 반환.
            String imageUrl = "/images/" + uniqueName;
            return ResponseEntity.ok(imageUrl);
        }catch (Exception e){
            return ResponseEntity.internalServerError().body("파일 업로드 실패");
        }
    }
}
