package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final S3Uploader s3Uploader;

  @Override
  @Transactional
  public BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage) {

    // ISBN 중복 체크
    if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
      throw new BusinessException(BookErrorCode.DUPLICATE_ISBN);
    }

    // 썸네일 이미지 업로드 (추후 구현)
    String thumbnailUrl = null;
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
       thumbnailUrl = s3Uploader.upload(thumbnailImage);
    }

    // 엔티티 생성
    Book book = createBookEntity(request, thumbnailUrl);

    // DB 저장
    Book savedBook = bookRepository.save(book);

    // DTO 변환, 반환
    return bookMapper.toDto(savedBook);
  }

  @Override
  public BookDto getBook(UUID bookId) {
    // 단건 조회
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

    return bookMapper.toDto(book);
  }

  @Override
  @Transactional
  public BookDto updateBook(UUID bookId, BookUpdateRequest request, MultipartFile thumbnailImage) {
    // 기존 도서 조회
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

    // MapStruct로 들어온 데이터 중 null이 아닌 값을 엔티티로 덮어씌움
    if (request != null) {
      bookMapper.updateBookFromRequest(request, book);
    }

    // 썸네일 이미지가 새로 갱신되면, S3에 업로드 후 URL 업데이트
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      String newThumbnailUrl = s3Uploader.upload(thumbnailImage);
      book.updateThumbnailUrl(newThumbnailUrl);
    }

    return bookMapper.toDto(book);
  }

  private Book createBookEntity(BookCreateRequest request, String thumbnailUrl) {
    return Book.builder()
        .title(request.title())
        .author(request.author())
        .isbn(request.isbn())
        .publisher(request.publisher())
        .description(request.description())
        .publishedDate(request.publishedDate())
        .thumbnailUrl(thumbnailUrl)
        .build();
  }
}
