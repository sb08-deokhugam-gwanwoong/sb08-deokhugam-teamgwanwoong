package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.NaverBookProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NaverBookTestController {

  private final NaverBookProvider naverBookProvider;

  @GetMapping("/test/naver")
  public NaverBookDto testNaver(@RequestParam String query) {
    return naverBookProvider.getBookMetadata(query);
  }

}
