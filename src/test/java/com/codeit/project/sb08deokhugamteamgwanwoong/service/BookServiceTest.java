package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.BookMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.external.S3Uploader;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.BookServiceImpl;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
}
