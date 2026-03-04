package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private final BookRepository bookRepository;

  @Override
  @Transactional
  public BookDto createBook(BookCreateRequest request, MultipartFile thumbnailImage) {

    // ISBN 중복 체크
    if (request.isbn() != null && bookRepository.existsByIsbn(request.isbn())) {
      throw new IllegalArgumentException("이미 존재하는 ISBN입니다.");
    }

    // 썸네일 이미지 업로드 (추후 구현)
    String thumbnailUrl = null;
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      // thumbnailUrl = s3Uploader.upload(thumbnailImage);
    }

    // 엔티티 생성
    Book book = Book.builder()
        .title(request.title())
        .author(request.author())
        .isbn(request.isbn())
        .publisher(request.publisher())
        .publishedDate(request.publishedDate())
        .description(request.description())
        .thumbnailUrl(thumbnailUrl)
        .build();

    // DB 저장
    Book savedBook = bookRepository.save(book);

    // DTO 변환, 반환
    return BookDto.builder()
        .id(savedBook.getId())
        .title(savedBook.getTitle())
        .author(savedBook.getAuthor())
        .isbn(savedBook.getIsbn())
        .publisher(savedBook.getPublisher())
        .publishedDate(savedBook.getPublishedDate())
        .description(savedBook.getDescription())
        .thumbnailUrl(savedBook.getThumbnailUrl())
        .reviewCount(savedBook.getReviewCount())
        .rating(savedBook.getRating())
        .createdAt(savedBook.getCreatedAt())
        .updatedAt(savedBook.getUpdatedAt())
        .build();
  }

}
