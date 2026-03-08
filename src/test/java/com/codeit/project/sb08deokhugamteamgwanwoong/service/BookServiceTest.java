package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.BookServiceImpl;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

  @InjectMocks
  private BookServiceImpl bookService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private S3Uploader s3Uploader;

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
}
