package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs.BookApi;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.CursorPageResponseBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.DashboardService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController implements BookApi {

  private final BookService bookService;
  private final DashboardService dashboardService;

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

  @GetMapping("/info")
  public ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @RequestParam("isbn") String isbn
  ) {
    log.info("Naver API 도서 정보 조회 요청 - ISBN: {}", isbn);
    NaverBookDto response = bookService.getBookInfoByIsbn(isbn);
    log.info("Naver API 도서 정보 조회 완료 - 도서 정보: {}", response.title());

    return ResponseEntity.status(HttpStatus.OK).body(response);
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
      @Valid @RequestPart(value = "bookData", required = false)BookUpdateRequest request,
      @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  ) {
    log.info("도서 정보 수정 요청 - bookId : {}", bookId);
    BookDto bookDto = bookService.updateBook(bookId, request, thumbnailImage);
    log.info("도서 정보 수정 완료 - bookId : {}", bookDto.id());

    return ResponseEntity.status(HttpStatus.OK).body(bookDto);
  }

  @DeleteMapping("/{bookId}")
  public ResponseEntity<Void> softDeleteBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 논리 삭제 요청 - bookId : {}", bookId);
    bookService.softDeleteBook(bookId);
    log.info("도서 논리 삭제 완료 - bookId : {}", bookId);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @DeleteMapping("/{bookId}/hard")
  public ResponseEntity<Void> hardDeleteBook(@PathVariable("bookId") UUID bookId) {
    log.info("도서 물리 삭제 요청 - bookId : {}", bookId);
    bookService.hardDeleteBook(bookId);
    log.info("도서 물리 삭제 완료 - bookId : {}", bookId);

    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseBookDto> searchBooks (
      @ModelAttribute BookPageRequest request
  ) {
    log.info("도서 목록 조회 요청 - keyword : {}, orderBy: {}, cursor : {}",
        request.keyword(), request.orderBy(), request.cursor());
    CursorPageResponseBookDto responseBookDto = bookService.searchBooks(request);
    log.info("도서 목록 조회 완료 - 반환된 데이터 개수: {}, 다음 페이지 존재 여부: {}",
        responseBookDto.content().size(), responseBookDto.hasNext());

    return ResponseEntity.status(HttpStatus.OK).body(responseBookDto);
  }

  @GetMapping("/popular")
  public ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @ModelAttribute DashboardPageRequest request
  ) {
    log.info("인기 도서 목록 조회 요청 - 기간: {}, cursor: {}, limit: {}",
        request.period(), request.cursor(), request.limit());

    CursorPageResponsePopularBookDto responseDto = dashboardService.getPopularBooks(request);
    log.info("인기 도서 목록 조회 완료 - 반환된 데이터 개수: {}, 다음 페이지 존재 여부: {}",
        responseDto.content().size(), responseDto.hasNext());

    return ResponseEntity.status(HttpStatus.OK).body(responseDto);
  }

  @PostMapping("/isbn/ocr")
  public ResponseEntity<String> getBookInfoByImage(
      @RequestParam("image") MultipartFile image
  ) {
    log.info("도서 이미지의 바코드/텍스트로 ISBN을 추출 요청");
    String isbn = bookService.getBookInfoByImage(image);

    // 200 ok와 함께 ISBN 문자열 반환
    return ResponseEntity.ok(isbn);
  }
}
