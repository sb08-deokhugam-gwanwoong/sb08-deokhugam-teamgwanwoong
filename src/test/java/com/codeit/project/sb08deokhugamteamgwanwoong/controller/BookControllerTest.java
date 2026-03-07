package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class BookControllerTest extends ControllerTestSupport {

  @DisplayName("도서 등록 API 호출 시 201 응답을 반환한다.")
  @Test
  void createBook_Success() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    // @RequestPart("bookData") 처리 JSON Mock 파일
    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    BookDto responseDto = BookDto.builder()
        .title("자바의 정석")
        .isbn("9788994492032")
        .build();
    given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(responseDto);

    // when & then
    mockMvc.perform(
        multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
    )
        .andDo(print())
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("자바의 정석"));
  }

  @DisplayName("이미 존재하는 ISBN으로 도서를 등록하면 409(Conflict) 에러를 반환한다.")
  @Test
  void createBook_Fail_DuplicateIsbn() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // 서비스 계층에서 예외가 발생하도록 Mocking
    given(bookService.createBook(any(), any()))
        .willThrow(new BusinessException(BookErrorCode.DUPLICATE_ISBN));

    // when & then
    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andDo(print())
        .andExpect(status().isConflict()); // 409 에러 검증
  }

  @DisplayName("도서 등록 API 호출 시 필수 값(제목)이 누락되면 400 응답을 반환한다.")
  @Test
  void createBook_Fail_BadRequest() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("") // 제목 누락
        .author("남궁성")
        .isbn("9788994492032")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // when & then
    mockMvc.perform(
        multipart("/api/books")
            .file(bookData)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
    )
        .andDo(print())
        .andExpect(status().isBadRequest()); // 400 검증
  }

  @DisplayName("도서 등록 중 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void createBook_Fail_InternalServerError() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // 서비스 계층에서 예기치 않은 런타임 에러가 발생하도록 Mocking
    given(bookService.createBook(any(), any())).willThrow(new RuntimeException("S3 연결 실패 등 예기치 않은 에러"));

    // when & then
    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 검증
  }
}
