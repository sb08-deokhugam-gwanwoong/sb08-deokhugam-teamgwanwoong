package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.CursorPageResponseBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.BookService;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.BookMetadataProvider;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService {

  private static final String NAVER_CLIENT_ID = "${naver.api.client-id:test-client-id}";
  private static final String NAVER_CLIENT_SECRET = "${naver.api.client-secret:test-client-secret}";
  private static final String NAVER_SEARCH_URL = "${naver.url.search.book:https://dummy.com}";

  private final BookRepository bookRepository;
  private final BookMapper bookMapper;
  private final S3Uploader s3Uploader;
  private final BookMetadataProvider<String> ocrSpaceBookProvider;
  private final RestTemplate restTemplate;

  @Value(NAVER_CLIENT_ID)
  private String naverClientId;
  @Value(NAVER_CLIENT_SECRET)
  private String naverClientSecret;
  @Value(NAVER_SEARCH_URL)
  private String naverBookSearchUrl;

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

  @Override
  @Cacheable(value = "naverBook", key = "#isbn", cacheManager = "redisCacheManager", unless = "#result == null")
  public NaverBookDto getBookInfoByIsbn(String isbn) {
    // 1. 네이버 API 요청 URL 구성
    // yaml에 설정된 기본 book.json을 ISBN 상세 검색용인 book_adv.json으로 변환하고 d_isbn 파라미터 적용
    String apiURL  = naverBookSearchUrl.replace("book.json", "book_adv.json") + "?d_isbn=" + isbn;

    // 2. HTTP 헤더에 인증 정보 세팅
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-Naver-Client-Id", naverClientId);
    headers.set("X-Naver-Client-Secret", naverClientSecret);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    try {
      // 3. 네이버 API 호출
      ResponseEntity<String> response = restTemplate.exchange(apiURL, HttpMethod.GET, entity, String.class);

      // 4. JSON 응답 파싱
      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode root = objectMapper.readTree(response.getBody());
      JsonNode items = root.path("items");

      // 검색 결과가 없는 경우 404 예외처리
      if (items.isEmpty()) {
        throw new BusinessException(BookErrorCode.BOOK_NOT_FOUND, "해당 ISBN의 도서 정보를 찾을 수 없습니다.");
      }

      // 5. 첫번째 결과 데이터 추출
      JsonNode bookNode = items.get(0);

      // 네이버 API는 출판일을 "20260310" 형태의 문자열로 응답하므로 "YYYY-MM-DD" 형태로 포맷팅
      String pubDateStr = bookNode.path("pubdate").asText();
      String formattedDate = "";
      if (pubDateStr.length() == 8) {
        formattedDate = pubDateStr.substring(0, 4) + "-" + pubDateStr.substring(4, 6) + "-" + pubDateStr.substring(6, 8);
      }

      String imageUrl = bookNode.path("image").asText();
      String base64Image = getBase64ImageFromUrl(imageUrl);

      // 6. DTO 변환 및 리턴
      return NaverBookDto.builder()
          .title(bookNode.path("title").asText())
          .author(bookNode.path("author").asText())
          .publisher(bookNode.path("publisher").asText())
          .description(bookNode.path("description").asText())
          .publishedDate(formattedDate)
          .isbn(bookNode.path("isbn").asText())
          .thumbnailImage(base64Image) // 네이버에서 제공하는 이미지 URL를 Base64 변환해서 삽입
          .build();

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      log.error("Naver API 호출 실패 - ISBN: {}, 원인: {}", isbn, e.getMessage());
      throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "네이버 도서 정보를 가져오는 중 서버 오류가 발생했습니다.");
    }

  }

  @Override
  public String getBookInfoByImage(MultipartFile image) {
    if (image == null || image.isEmpty()) {
      throw new BusinessException(GlobalErrorCode.INVALID_INPUT, "이미지 파일이 비어있습니다.");
    }

    try {
      // 1. 이미지를 Base64 문자열로 변환
      String base64Content = Base64.getEncoder().encodeToString(image.getBytes());
      String mimeType = image.getContentType();
      String base64Image = "data:" + mimeType + ";base64,"+ base64Content;

      // 2. 외부 API 호출 담당 Provider에 위임
      return ocrSpaceBookProvider.getBookMetadata(base64Image);

    } catch (Exception e) {
      throw new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "이미지 처리 중 서버 오류가 발생했습니다.");
    }
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

  // 이미지 URL을 읽어서 Base64 인코딩된 문자열로 변환하는 헬퍼 메서드
  private String getBase64ImageFromUrl(String imageUrl) {
    if (imageUrl == null || imageUrl.isBlank()) {
      return null;
    }
    try {
      byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
      if (imageBytes != null) {
        return Base64.getEncoder().encodeToString(imageBytes);
      }
    } catch (Exception e) {
      log.warn("이미지 다운로드 및 Base64 변환 실패 - URL: {}", imageUrl, e);
    }
    return null;
  }
}
