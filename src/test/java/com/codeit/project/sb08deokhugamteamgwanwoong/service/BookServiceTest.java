package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.OcrSpaceBookProvider;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.BookServiceImpl;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

  @InjectMocks
  private BookServiceImpl bookService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private S3Uploader s3Uploader;

  @Mock
  private OcrSpaceBookProvider ocrSpaceBookProvider;

  @Spy
  private BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

  /*
   * 도서 등록 관련 테스트
   * */
  @DisplayName("도서를 정상적으로 등록할 수 있다. (이미지 포함)")
  @Test
  void createBook_Success_WithImage() {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage", "image.jpg", "image/jpeg", "dummy content".getBytes()
    );
    String expectedUrl = "http://s3-bucket.com/image.jpg";

    // ISBN 중복 체크 mocking
    given(bookRepository.existsByIsbn(request.isbn())).willReturn(false);

    // S3 업로드 mocking
    given(s3Uploader.upload(any(MultipartFile.class))).willReturn(expectedUrl);

    // save 메서드 mocking -> 저장된 객체 반환
    given(bookRepository.save(any(Book.class))).willAnswer(invocation -> {
      Book book = invocation.getArgument(0);
      return book;
    });

    // when
    BookDto bookDto = bookService.createBook(request, thumbnailImage);

    // then
    assertThat(bookDto.title()).isEqualTo("자바의 정석");
    assertThat(bookDto.isbn()).isEqualTo("9788994492032");
    assertThat(bookDto.thumbnailUrl()).isEqualTo(expectedUrl); // URL 확인

    // save 메서드가 호출 됐는지 검증
    verify(bookRepository).save(any(Book.class));
    verify(s3Uploader).upload(any(MultipartFile.class)); // 업로드 호출 확인
  }

  @DisplayName("이미지가 없어도 도서를 등록할 수 있다.")
  @Test
  void createBook_Success_NoImage() {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MultipartFile thumbnailImage = null; // 이미지 없음

    // ISBN 중복 체크 mocking
    given(bookRepository.existsByIsbn(request.isbn())).willReturn(false);

    // save 메서드 mocking
    given(bookRepository.save(any(Book.class))).willAnswer(invocation -> {
      Book book = invocation.getArgument(0);
      return book;
    });

    // Mapper mocking (thumbnailUrl -> null)
    given(bookMapper.toDto(any(Book.class))).willAnswer(invocation -> {
      Book book = invocation.getArgument(0);
      return BookDto.builder()
          .title(book.getTitle())
          .isbn(book.getIsbn())
          .thumbnailUrl(book.getThumbnailUrl()) // null 상태
          .build();
    });

    // when
    BookDto bookDto = bookService.createBook(request, thumbnailImage);

    // then
    assertThat(bookDto.title()).isEqualTo("자바의 정석");
    assertThat(bookDto.thumbnailUrl()).isNull(); // URL이 없는지 확인

    // verify
    verify(bookRepository).save(any(Book.class));
    // s3Uploader.upload()가 호출되지 않았는지 검증
    verify(s3Uploader, never()).upload(any());
  }


  @DisplayName("중복된 ISBN으로 도서를 등록하면 예외가 발생한다.")
  @Test
  void createBook_DuplicateIsbn() {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    // ISBN이 이미 존재할 때
    given(bookRepository.existsByIsbn(request.isbn())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> bookService.createBook(request, null))
        .isInstanceOf(BusinessException.class) // 커스텀 예외 처리 예정
        .hasMessageContaining("이미 존재하는 ISBN입니다.");
  }

  /*
   * 네이버 도서 검색 API 연동 테스트
   * */
  @DisplayName("네이버 API를 통해 ISBN으로 도서 정보를 성공적으로 가져온다. (이미지 Base64 변환 포함)")
  @Test
  void getBookInfoByIsbn_Success() {
    // given
    String isbn = "9788994492032";

    // @Value 강제 주입 (단위 테스트 환경)
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");
    ReflectionTestUtils.setField(bookService, "naverClientId", "test-client-id");
    ReflectionTestUtils.setField(bookService, "naverClientSecret", "test-client-secret");

    // 네이버에서 줄 가짜 JSON 응답
    String mockJsonResponse = """
        {
          "items": [
            {
              "title": "자바의 정석",
              "author": "남궁성",
              "publisher": "도우출판",
              "description": "자바 기본서",
              "pubdate": "20160127",
              "isbn": "9788994492032",
              "image": "https://example.com/image.jpg"
            }
          ]
        }
        """;

    // 네이버에서 다운받을 가짜 이미지 바이트 배열
    byte[] mockImageBytes = "dummy image content".getBytes();

    // RestTemplate 생성자 가로채기
    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          // 1. 도서 정보 조회 (exchange) 호출 시 가짜 JSON 반환
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

          // 2. 이미지 다운로드 (getForObject) 호출 시 가짜 바이트 배열 반환
          when(mock.getForObject(anyString(), eq(byte[].class)))
              .thenReturn(mockImageBytes);
        })) {

      // when
      NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

      // then
      assertThat(result.title()).isEqualTo("자바의 정석");
      assertThat(result.author()).isEqualTo("남궁성");
      assertThat(result.publishedDate()).isEqualTo("2016-01-27"); // 날짜 포맷팅 검증

      // 이미지가 Base64로 잘 인코딩되었는지 검증
      String expectedBase64 = Base64.getEncoder().encodeToString(mockImageBytes);
      assertThat(result.thumbnailImage()).isEqualTo(expectedBase64);
    }
  }

  @DisplayName("네이버 API 검색 결과가 비어있으면 BOOK_NOT_FOUND 예외를 던진다.")
  @Test
  void getBookInfoByIsbn_Fail_EmptyItems() {
    // given
    String isbn = "1234567890123";
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");

    // items 배열이 비어있는 가짜 JSON 응답
    String mockEmptyResponse = "{ \"items\": [] }";

    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenReturn(new ResponseEntity<>(mockEmptyResponse, HttpStatus.OK));
        })) {

      // when & then
      BusinessException exception = assertThrows(BusinessException.class, () -> {
        bookService.getBookInfoByIsbn(isbn);
      });
      assertThat(exception.getErrorCode()).isEqualTo(BookErrorCode.BOOK_NOT_FOUND);
    }
  }

  @DisplayName("네이버 API 연동 중 예기치 않은 통신 에러가 발생하면 INTERNAL_SERVER_ERROR 예외를 던진다.")
  @Test
  void getBookInfoByIsbn_Fail_InternalError() {
    // given
    String isbn = "9788994492032";
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");

    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          // 통신 중 런타임 에러(예: 타임아웃)가 터지는 상황 가정
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenThrow(new RuntimeException("네이버 서버 연결 시간 초과"));
        })) {

      // when & then
      BusinessException exception = assertThrows(BusinessException.class, () -> {
        bookService.getBookInfoByIsbn(isbn);
      });
      assertThat(exception.getErrorCode()).isEqualTo(GlobalErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  @DisplayName("네이버 API 응답에 이미지 URL이 비어있으면(blank) 썸네일은 null을 반환한다.")
  @Test
  void getBookInfoByIsbn_Success_BlankImageUrl() {
    // given
    String isbn = "9788994492032";
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");

    // image 필드가 아예 비어있는 가짜 JSON 응답
    String mockJsonResponse = """
        {
          "items": [
            {
              "title": "자바의 정석",
              "isbn": "9788994492032",
              "image": "" 
            }
          ]
        }
        """;

    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));
        })) {

      // when
      NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

      // then
      // imageUrl.isBlank() 분기를 타서 null이 반환되어야 함
      assertThat(result.title()).isEqualTo("자바의 정석");
      assertThat(result.thumbnailImage()).isNull();
    }
  }

  @DisplayName("이미지 다운로드 결과(byte 배열)가 null이면 썸네일은 null을 반환한다.")
  @Test
  void getBookInfoByIsbn_Success_NullImageBytes() {
    // given
    String isbn = "9788994492032";
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");

    // 정상적인 이미지 URL이 포함된 JSON 응답
    String mockJsonResponse = "{ \"items\": [ { \"title\": \"자바의 정석\", \"isbn\": \"9788994492032\", \"image\": \"https://example.com/image.jpg\" } ] }";

    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

          // RestTemplate이 이미지를 다운받지 못하고 null을 반환하는 상황 mocking
          when(mock.getForObject(anyString(), eq(byte[].class)))
              .thenReturn(null);
        })) {

      // when
      NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

      // then
      // imageBytes != null 분기의 false를 타서 맨 아래 return null이 실행되어야 함
      assertThat(result.thumbnailImage()).isNull();
    }
  }

  @DisplayName("이미지 다운로드 중 예외가 발생하면(catch), 에러를 반환하고 썸네일은 null을 반환한다.")
  @Test
  void getBookInfoByIsbn_Success_ImageDownloadException() {
    // given
    String isbn = "9788994492032";
    ReflectionTestUtils.setField(bookService, "naverBookSearchUrl", "https://openapi.naver.com/v1/search/book.json");

    String mockJsonResponse = "{ \"items\": [ { \"title\": \"자바의 정석\", \"isbn\": \"9788994492032\", \"image\": \"https://example.com/image.jpg\" } ] }";

    try (MockedConstruction<RestTemplate> mocked = mockConstruction(RestTemplate.class,
        (mock, context) -> {
          when(mock.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
              .thenReturn(new ResponseEntity<>(mockJsonResponse, HttpStatus.OK));

          // RestTemplate이 이미지를 받다가 타임아웃/404 등 런타임 예외를 터뜨리는 상황 mocking
          when(mock.getForObject(anyString(), eq(byte[].class)))
              .thenThrow(new RuntimeException("이미지 서버 연결 실패"));
        })) {

      // when
      NaverBookDto result = bookService.getBookInfoByIsbn(isbn);

      // then
      // catch (Exception e) 블록을 타서 로그를 찍고 맨 아래 return null이 실행되어야 함
      // 전체 API 로직은 실패하지 않고 도서 정보는 정상 반환되어야 함
      assertThat(result.title()).isEqualTo("자바의 정석");
      assertThat(result.thumbnailImage()).isNull();
    }
  }
  /*
   * 도서 상세 조회 관련 테스트
   * */
  @DisplayName("존재하는 도서 ID로 조회하면 도서 정보를 정상적으로 반환한다.")
  @Test
  void getBook_Success() {
    // given
    UUID bookId = UUID.randomUUID();
    Book book = Book.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .build();

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    // when
    BookDto bookDto = bookService.getBook(bookId);

    // then
    assertThat(bookDto.title()).isEqualTo("자바의 정석");
    verify(bookRepository).findById(bookId);
  }

  @DisplayName("존재하지 않는 도서 ID로 조회하면 예외가 발생한다.")
  @Test
  void getBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.getBook(bookId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.BOOK_NOT_FOUND.getMessage());
  }

  /*
   * 도서 정보 수정 관련 테스트
   * */
  @DisplayName("도서 정보를 수정할 때 새로운 이미지가 있으면 기존 S3 이미지를 지우고 새 이미지를 업로드한다.")
  @Test
  void updateBook_Success_WithNewImage() {
    // given
    UUID bookId = UUID.randomUUID();
    Book book = Book.builder()
        .title("기존 제목")
        .author("기존 저자")
        .thumbnailUrl("http://s3.com/old-image.jpg") // 기존 이미지가 있었음
        .build();

    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 제목")
        .author("수정된 저자")
        .build();

    MockMultipartFile newThumbnailImage = new MockMultipartFile(
        "thumbnailImage",
        "new.jpg",
        MediaType.IMAGE_JPEG_VALUE,
        "new content".getBytes()
    );
    String newExpectedUrl = "http://s3.com/new-image.jpg";

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
    given(s3Uploader.upload(any(MultipartFile.class))).willReturn(newExpectedUrl);

    // when
    BookDto bookDto = bookService.updateBook(bookId, request, newThumbnailImage);

    // then
    assertThat(bookDto.title()).isEqualTo("수정된 제목");
    assertThat(book.getThumbnailUrl()).isEqualTo(newExpectedUrl); // 엔티티 URL 변경 확인

    // 검증. 기존 이미지가 삭제 됐는지, 새 이미지가 업로드 되었는지
    verify(s3Uploader).delete("http://s3.com/old-image.jpg");
    verify(s3Uploader).upload(any(MultipartFile.class));
  }

  @DisplayName("도서 정보를 수정할 때 이미지가 없으면 S3 업로드/삭제 로직을 타지 않는다.")
  @Test
  void updateBook_Success_NoImage() {
    // given
    UUID bookId = UUID.randomUUID();
    Book book = Book.builder()
        .title("기존 제목")
        .author("기존 저자")
        .thumbnailUrl("http://s3.com/old-image.jpg")
        .build();

    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 제목")
        .author("수정된 저자")
        .build();

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    // when
    BookDto bookDto = bookService.updateBook(bookId, request, null); // 이미지 null 전달

    // then
    assertThat(bookDto.title()).isEqualTo("수정된 제목");
    assertThat(book.getThumbnailUrl()).isEqualTo("http://s3.com/old-image.jpg"); // 기존 이미지 유지 확인

    // 검증. S3 로직이 호출되지 않았음을 확인
    verify(s3Uploader, never()).delete(any());
    verify(s3Uploader, never()).upload(any());
  }

  @DisplayName("존재하지 않는 도서를 수정하려 하면 예외가 발생한다.")
  @Test
  void updateBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder().title("수정된 제목").build();

    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.updateBook(bookId, request, null))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.BOOK_NOT_FOUND.getMessage());
  }

  // 엔티티 도메인 테스트에서 부족한 커버리지 내용 추가
  @DisplayName("[Domain Test] 도서 정보를 업데이트한다. (null 파라미터는 기존 값을 유지한다)")
  @Test
  void update_BookEntity() {
    Book book = Book.builder().title("기존 제목").author("기존 저자").build();
    BookUpdateRequest request = BookUpdateRequest.builder().title("수정된 제목").author(null).build();

    book.update(request);

    assertThat(book.getTitle()).isEqualTo("수정된 제목");
    assertThat(book.getAuthor()).isEqualTo("기존 저자"); // 유지됨
  }

  @DisplayName("[Domain Test] 썸네일 이미지가 null일 경우 업데이트하지 않고, 값이 있으면 업데이트한다.")
  @Test
  void updateThumbnailUrl_BookEntity() {
    Book book = Book.builder().thumbnailUrl("old.jpg").build();

    book.updateThumbnailUrl(null);
    assertThat(book.getThumbnailUrl()).isEqualTo("old.jpg");

    book.updateThumbnailUrl("new.jpg");
    assertThat(book.getThumbnailUrl()).isEqualTo("new.jpg");
  }

  /*
   * 도서 삭제(논리/물리) 관련 테스트
   * */
  @DisplayName("존재하는 도서를 논리 삭제하면 정상적으로 처리된다.")
  @Test
  void softDeleteBook_Success() {
    // given
    UUID bookId = UUID.randomUUID();
    Book book = Book.builder()
        .title("자바의 정석")
        .build();

    given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

    // when
    bookService.softDeleteBook(bookId);

    // then
    // void 메서드이므로 에러가 터지지 않고 레포지토리의 findById가 정상 호출되었는지 검증
    verify(bookRepository).findById(bookId);
  }

  @DisplayName("존재하지 않는 도서를 논리 삭제하려 하면 예외가 발생한다.")
  @Test
  void softDeleteBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findById(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.softDeleteBook(bookId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.BOOK_NOT_FOUND.getMessage());
  }

  @DisplayName("썸네일이 있는 도서를 물리 삭제하면 S3 이미지와 DB 데이터가 모두 삭제된다.")
  @Test
  void hardDeleteBook_Success_WithImage() {
    // given
    UUID bookId = UUID.randomUUID();
    String thumbnailUrl = "http://s3.com/image.jpg";
    Book book = Book.builder()
        .title("자바의 정석")
        .thumbnailUrl(thumbnailUrl) // S3에 이미지가 있는 상태
        .build();

    // 물리 삭제는 findByIdIncludeDeleted 를 사용하므로 이 부분 mocking
    given(bookRepository.findByIdIncludeDeleted(bookId)).willReturn(Optional.of(book));

    // when
    bookService.hardDeleteBook(bookId);

    // then
    // 검증: S3 이미지 삭제와 DB 레코드 삭제가 모두 정확히 1번씩 호출되었는가
    verify(s3Uploader).delete(thumbnailUrl);
    verify(bookRepository).hardDeleteById(bookId);
  }

  @DisplayName("썸네일이 없는 도서를 물리 삭제하면 S3 삭제 로직은 타지 않고 DB 데이터만 삭제된다.")
  @Test
  void hardDeleteBook_Success_NoImage() {
    // given
    UUID bookId = UUID.randomUUID();
    Book book = Book.builder()
        .title("자바의 정석")
        .thumbnailUrl(null) // 이미지가 없는 상태
        .build();

    given(bookRepository.findByIdIncludeDeleted(bookId)).willReturn(Optional.of(book));

    // when
    bookService.hardDeleteBook(bookId);

    // then
    // 검증. S3 삭제 메서드는 절대 호출되지 않아야 하고, DB 삭제만 호출되어야 함
    verify(s3Uploader, never()).delete(any());
    verify(bookRepository).hardDeleteById(bookId);
  }

  @DisplayName("존재하지 않는 도서를 물리 삭제하려 하면 예외가 발생한다.")
  @Test
  void hardDeleteBook_Fail_NotFound() {
    // given
    UUID bookId = UUID.randomUUID();
    given(bookRepository.findByIdIncludeDeleted(bookId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> bookService.hardDeleteBook(bookId))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(BookErrorCode.BOOK_NOT_FOUND.getMessage());
  }

  /*
   * 도서 목록 검색 (커서 페이징) 관련 테스트
   * */
  @DisplayName("도서 목록 검색 시 요청한 limit보다 많은 데이터가 조회되면 hasNext가 true가 되고 초과된 데이터는 잘라낸다.")
  @Test
  void searchBooks_HasNext_True() {
    // given
    // 프론트엔드에서 limit=2를 요청한 상황
    BookPageRequest bookPageRequest = new BookPageRequest("자바", "title", "DESC", null, null, 2);

    // DB는 createdAt 값이 필수이기 때문에 mock 데이터에도 세팅해줌
    Book book1 = Book.builder().title("자바1").build();
    Book book2 = Book.builder().title("자바2").build();
    Book book3 = Book.builder().title("자바3").build(); // limit 2보다 1개 더 조회

    // 서비스는 limit + 1 = 3개를 요청. 리포지토리는 3개를 반환하도록 mocking
    // 서비스 내부에서 .remove()를 사용하므로, 변경 가능한 ArrayList로 묶어서 반환
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>(List.of(book1, book2, book3)));

    // Mapper가 엔티티를 DTO로 변환하도록 mocking
    given(bookMapper.toDto(any(Book.class))).willAnswer(invocation -> {
      Book b = invocation.getArgument(0);
      return BookDto.builder()
          .title(b.getTitle())
          .createdAt(Instant.now())
          .build();
    });

    // when
    CursorPageResponseBookDto pageResponseBookDto = bookService.searchBooks(bookPageRequest);

    // then
    assertThat(pageResponseBookDto.hasNext()).isTrue(); // 다음 페이지가 있다고 판단
    assertThat(pageResponseBookDto.content()).hasSize(2); // 프론트엔드에는 2개만 잘라서 반환
    assertThat(pageResponseBookDto.content().get(0).title()).isEqualTo("자바1");
    assertThat(pageResponseBookDto.content().get(1).title()).isEqualTo("자바2");
  }

  @DisplayName("도서 목록 검색 시 요청한 limit 이하의 데이터가 조회되면 hasNext가 false이다.")
  @Test
  void searchBooks_HasNext_False() {
    // given
    BookPageRequest bookPageRequest = new BookPageRequest("스프링", "createdAt", "DESC", null, null, 10);

    Book book1 = Book.builder().title("스프링1").build();

    // limit = 10인데, DB에는 1개만 조회된 상황 mocking
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>(List.of(book1)));

    given(bookMapper.toDto(any(Book.class))).willAnswer(invocation -> {
      Book b = invocation.getArgument(0);
      return BookDto.builder().title(b.getTitle()).createdAt(Instant.now()).build();
    });

    // when
    CursorPageResponseBookDto responseBookDto = bookService.searchBooks(bookPageRequest);

    // then
    assertThat(responseBookDto.hasNext()).isFalse(); // 뒤에 데이터가 더 없다고 판단
    assertThat(responseBookDto.content()).hasSize(1);
  }

  @DisplayName("도서 목록 검색 시 평점순(rating) 정렬과 after 파라미터가 정상적으로 파싱 및 적용된다.")
  @Test
  void searchBooks_OrderByRating_WithAfter() {
    // given
    // after에 날짜 문자열을 넣어 Instant.parse() 로직을 타게 유도
    BookPageRequest request = new BookPageRequest("자바", "rating", "DESC", "4.5", "2026-03-09T10:00:00Z", 10);

    Book book = Book.builder().title("자바의 정석").build();
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>(List.of(book)));

    // rating이 null인 상황을 만들어 삼항 연산자(?:)의 false 분기 테스트
    given(bookMapper.toDto(any(Book.class))).willReturn(
        BookDto.builder().title("자바의 정석").rating(null).createdAt(Instant.now()).build()
    );

    // when
    CursorPageResponseBookDto response = bookService.searchBooks(request);

    // then
    assertThat(response.nextCursor()).isEqualTo("0.0"); // null일 때 0.0으로 셋팅되는지 확인
    assertThat(response.content()).hasSize(1);
  }

  @DisplayName("도서 목록 검색 시 리뷰개수순(reviewCount) 정렬이 정상적으로 적용된다.")
  @Test
  void searchBooks_OrderByReviewCount() {
    // given
    BookPageRequest request = new BookPageRequest("자바", "reviewCount", "DESC", null, null, 10);

    Book book = Book.builder().title("자바의 정석").build();
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>(List.of(book)));

    // reviewCount가 null인 상황
    given(bookMapper.toDto(any(Book.class))).willReturn(
        BookDto.builder().title("자바의 정석").reviewCount(null).createdAt(Instant.now()).build()
    );

    // when
    CursorPageResponseBookDto response = bookService.searchBooks(request);

    // then
    assertThat(response.nextCursor()).isEqualTo("0"); // null일 때 0으로 셋팅되는지 확인
  }

  @DisplayName("도서 목록 검색 시 출판일순(publishedDate) 정렬이 정상적으로 적용된다.")
  @Test
  void searchBooks_OrderByPublishedDate() {
    // given
    BookPageRequest request = new BookPageRequest("자바", "publishedDate", "DESC", null, null, 10);

    Book book = Book.builder().title("자바의 정석").build();
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>(List.of(book)));

    LocalDate pubDate = LocalDate.of(2023, 1, 1);
    given(bookMapper.toDto(any(Book.class))).willReturn(
        BookDto.builder().title("자바의 정석").publishedDate(pubDate).createdAt(Instant.now()).build()
    );

    // when
    CursorPageResponseBookDto response = bookService.searchBooks(request);

    // then
    assertThat(response.nextCursor()).isEqualTo(pubDate.toString()); // 날짜가 문자열로 잘 변환되는지 확인
  }

  @DisplayName("도서 목록 검색 결과가 없을 경우 빈 리스트와 함께 nextCursor/nextAfter가 null로 반환된다.")
  @Test
  void searchBooks_EmptyResult() {
    // given
    BookPageRequest request = new BookPageRequest("세상에없는책", "title", "DESC", null, null, 10);

    // DB 조회 결과가 아예 없는 상황(빈 리스트) mocking
    given(bookRepository.findAllByCursor(any(BookSearchCondition.class), any(Pageable.class)))
        .willReturn(new ArrayList<>());

    // when
    CursorPageResponseBookDto response = bookService.searchBooks(request);

    // then
    // isEmpty() = true 분기를 타서 커서들이 null로 유지되는지 확인
    assertThat(response.content()).isEmpty();
    assertThat(response.nextCursor()).isNull();
    assertThat(response.nextAfter()).isNull();
    assertThat(response.hasNext()).isFalse();
  }

  /*
   * Book 엔티티 내부 비즈니스 로직(도메인) 단위 테스트
   * 원래는 BookTest.java로 분리하는 것이 좋으나, 편의상 통합하여 작성함
   * 리뷰 관련 테스트
   */
  @DisplayName("[Domain Test] 리뷰 개수를 증가 및 감소시킨다. (0 이하로는 감소하지 않음)")
  @Test
  void reviewCount_Increase_Decrease_BookEntity() {
    Book book = Book.builder().build(); // 기본 reviewCount = 0
    ReflectionTestUtils.setField(book, "reviewCount", 0);

    book.decreaseReviewCount(); // 방어 로직
    assertThat(book.getReviewCount()).isEqualTo(0);

    book.increaseReviewCount();
    assertThat(book.getReviewCount()).isEqualTo(1);

    book.decreaseReviewCount();
    assertThat(book.getReviewCount()).isEqualTo(0);
  }

  @DisplayName("[Domain Test] 리뷰가 삭제될 때 평점과 개수가 올바르게 업데이트된다.")
  @Test
  void removeReviewRating_BookEntity() {
    Book book = Book.builder().build();
    ReflectionTestUtils.setField(book, "rating", 4.5);
    ReflectionTestUtils.setField(book, "reviewCount", 2);

    book.removeReviewRating(4); // 4점짜리 삭제 (else 분기)
    assertThat(book.getReviewCount()).isEqualTo(1);
    assertThat(book.getRating()).isEqualTo(5.0);

    book.removeReviewRating(5); // 나머지 1개 삭제 (if 분기)
    assertThat(book.getReviewCount()).isEqualTo(0);
    assertThat(book.getRating()).isEqualTo(0.0);
  }

  @DisplayName("[Domain Test] 리뷰 별점이 수정될 때 평점이 올바르게 다시 계산된다.")
  @Test
  void updateReviewRating_BookEntity() {
    Book book = Book.builder().build();
    ReflectionTestUtils.setField(book, "reviewCount", 0);

    book.updateReviewRating(5, 1); // 0개일 때 방어 로직
    assertThat(book.getRating()).isEqualTo(0.0);

    ReflectionTestUtils.setField(book, "reviewCount", 2);
    ReflectionTestUtils.setField(book, "rating", 4.0); // 4, 4

    book.updateReviewRating(4, 5); // 4 -> 5로 수정 (총점 9)
    assertThat(book.getRating()).isEqualTo(4.5);
  }

  /*
  * 도서 이미지 기반 ISBN 조회 테스트
  * */
  @DisplayName("이미지 파일을 받으면 Base64로 변환하여 OCR Provider를 호출하고 ISBN을 반환한다.")
  @Test
  void getBookInfoByImage_Success() throws Exception {
    // given
    byte[] imageBytes = "dummy image content".getBytes();
    MockMultipartFile image = new MockMultipartFile(
        "image", "barcode.png", "image/png", imageBytes
    );

    String expectedIsbn = "9788994492032";

    // Service에서는 순수하게 Provider 호출만 mocking
    given(ocrSpaceBookProvider.getBookMetadata(anyString())).willReturn(expectedIsbn);

    // when
    String isbn = bookService.getBookInfoByImage(image);

    // then
    assertThat(isbn).isEqualTo(expectedIsbn);
    verify(ocrSpaceBookProvider).getBookMetadata(anyString());
  }

  @DisplayName("입력된 이미지 파일이 null이거나 비어있으면 INVALID_INPUT 에러를 던진다.")
  @Test
  void getBookInfoByImage_Fail_EmptyImage() {
    // given
    MockMultipartFile emptyImage = new MockMultipartFile(
        "image", "empty.png", "image/png", new byte[0]
    );

    // when & then
    assertThatThrownBy(() -> bookService.getBookInfoByImage(emptyImage))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("이미지 파일이 비어있습니다.")
        .extracting("errorCode").isEqualTo(GlobalErrorCode.INVALID_INPUT);

    // Provider가 절대 호출되지 않아야 함 검증
    verify(ocrSpaceBookProvider, never()).getBookMetadata(anyString());
  }

  @DisplayName("이미지 처리 중 Provider에서 에러가 발생하면 그대로 예외를 던지거나 감싸서 던진다.")
  @Test
  void getBookInfoByImage_Fail_InternalError() {
    // given
    MockMultipartFile image = new MockMultipartFile(
        "image", "barcode.png", "image/png", "dummy".getBytes()
    );

    // Provider에서 에러 발생 가정
    given(ocrSpaceBookProvider.getBookMetadata(anyString()))
        .willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR));

    // when & then
    assertThatThrownBy(() -> bookService.getBookInfoByImage(image))
        .isInstanceOf(BusinessException.class);
  }
}
