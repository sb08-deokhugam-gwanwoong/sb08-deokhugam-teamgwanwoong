package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.CursorPageResponseBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    // 썸네일 이미지 업로드
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
      book.update(request);
    }

    // 썸네일 이미지가 새로 갱신되면, S3에 업로드 후 URL 업데이트
    if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
      // 새로운 이미지를 올리기 전에, 기존 이미지가 있단면 S3에서 먼저 삭제
      if (book.getThumbnailUrl() != null) {
        s3Uploader.delete(book.getThumbnailUrl());
      }

      String newThumbnailUrl = s3Uploader.upload(thumbnailImage);
      book.updateThumbnailUrl(newThumbnailUrl);
    }

    return bookMapper.toDto(book);
  }

  @Override
  @Transactional
  public void softDeleteBook(UUID bookId) {
    // 기존 도서 조회
   Book book = bookRepository.findById(bookId)
       .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

   // soft-delete
   book.delete();
  }

  @Override
  @Transactional
  public void hardDeleteBook(UUID bookId) {
   // 논리 삭제된 도서인지 확인
   Book book = bookRepository.findByIdIncludeDeleted(bookId)
       .orElseThrow(() -> new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

   // S3에 올라가 있는 썸네일 이미지도 같이 삭제
   if (book.getThumbnailUrl() != null) {
     s3Uploader.delete(book.getThumbnailUrl());
   }

   bookRepository.hardDeleteById(bookId);
  }

  @Override
  public CursorPageResponseBookDto searchBooks(BookPageRequest request) {
    // 프론트엔드가 보낸 문자열 방향(ASC/DESC)을 Spring의 Sort.Direction 객체로 변환
    Sort.Direction direction = Sort.Direction.fromString(request.direction());

    // e다음 페이지가 있는지(hasNext)를 알아내기 위해 요청 limit보다 1개 더 조회
    int limit = request.limit();
    Pageable pageable = PageRequest.of(0, limit + 1);

    // after 값이 문자열로 넘어오면 Instant 파싱
    Instant afterInstant = null;
    if (request.after() != null && !request.after().isBlank()) {
      afterInstant = Instant.parse(request.after());
    }

    // Repository에 던져줄 검색 조건
    BookSearchCondition condition = BookSearchCondition.builder()
        .keyword(request.keyword())
        .cursor(request.cursor())
        .after(afterInstant)
        .orderBy(request.orderBy())
        .direction(direction)
        .build();

    // QueryDSL로 데이터 가져오기
    List<Book> bookList = bookRepository.findAllByCursor(condition, pageable);

    // hasNext 존재하는지 확인 후 초과한 값 잘라내기
    boolean hasNext = false;
    if (bookList.size() > limit) {
      hasNext = true;
      // 프론트로 다시 넘겨줄 땐 limit만큼 다시 잘라서 줌
      bookList.remove(bookList.size() - 1);
    }

    // Entity -> DTO 변환
    List<BookDto> bookDtoList = bookList.stream()
        .map(bookMapper::toDto)
        .toList();

    // 다음 API 호출 시 FE에서 사용할 nextCursor, nextAfter 계산
    String nextCursor = null;
    Instant nextAfter = null;

    if (!bookDtoList.isEmpty()) {
      // 현재 페이지의 맨 마지막 도서 추출
      BookDto lastBook = bookDtoList.get(bookDtoList.size() - 1);

      // 2순위 고유 커서(after) ->무조건 생성 시간
      nextAfter = lastBook.createdAt();

      // 1순위 커서는 사용자가 요청한 정렬 기준(orderBy)에 따라서 세팅
      if ("rating".equals(request.orderBy())) {
        nextCursor = String.valueOf(lastBook.rating() != null ? lastBook.rating() : 0.0);
      } else if ("reviewCount".equals(request.orderBy())) {
        nextCursor = String.valueOf(lastBook.reviewCount() != null ? lastBook.reviewCount() : 0);
      } else if ("publishedDate".equals(request.orderBy())) {
        nextCursor = lastBook.publishedDate() != null ? lastBook.publishedDate().toString() : null;
      } else if ("title".equals(request.orderBy())) {
        nextCursor = lastBook.title();
      } else {
        nextCursor = lastBook.createdAt().toString(); // 기본 정렬(createdAt)인 경우
      }
    }

    // 최종 응답 DTO로 반환
    return CursorPageResponseBookDto.builder()
        .content(bookDtoList)
        .nextCursor(nextCursor)
        .nextAfter(nextAfter)
        .size(limit)
        .totalElements(null) // 무한 스크롤에서 전체 개수(Count 쿼리)를 세지 않는게 성능상 좋으므로 null 처리
        .hasNext(hasNext)
        .build();
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
