package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

  private final BookService bookService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> createBook(
      @Valid @RequestPart("bookData")BookCreateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    boolean hasImage = (thumbnailImage != null && !thumbnailImage.isEmpty());

    log.info("도서 등록 요청 - 도서 제목 : {}, ISBN : {}, hasImage : {}", request.title(), request.isbn(), hasImage);
    BookDto bookDto = bookService.createBook(request, thumbnailImage);
    log.info("도서 등록 완료 - 등록된 도서 ID : {}", bookDto.id());

    return ResponseEntity.status(HttpStatus.CREATED).body(bookDto);
  }

  @GetMapping("/{bookId}")
  public ResponseEntity<BookDto> getBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 상세 조회 요청 - bookId: {}", bookId);
    BookDto bookDto = bookService.getBook(bookId);
    log.info("도서 상세 조회 완료 - 도서 정보: {}", bookDto.title());

    return ResponseEntity.status(HttpStatus.OK).body(bookDto);
  }

  @PatchMapping(value = "/{bookId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<BookDto> updateBook(
      @PathVariable("bookId") UUID bookId,
      @RequestPart(value = "bookData", required = false)BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    log.info("도서 정보 수정 요청 - bookId : {}", bookId);
    BookDto bookDto = bookService.updateBook(bookId, request, thumbnailImage);
    log.info("도서 정보 수정 완료 - bookId : {}", bookDto.id());

    return ResponseEntity.status(HttpStatus.OK).body(bookDto);
  }
}
