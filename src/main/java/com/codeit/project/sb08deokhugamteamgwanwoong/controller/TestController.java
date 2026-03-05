package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.NaverBookProvider;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class TestController {

  private final NaverBookProvider naverBookProvider;
  private final S3Uploader s3Uploader;

  @GetMapping("/test/naver")
  public NaverBookDto testNaver(@RequestParam String query) {
    return naverBookProvider.getBookMetadata(query);
  }

  @PostMapping("/test/upload")
  public String testUpload(@RequestPart MultipartFile file) {
    return s3Uploader.upload(file);
  }

}
