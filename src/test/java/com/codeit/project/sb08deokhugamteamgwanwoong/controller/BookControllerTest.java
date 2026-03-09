package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.CursorPageResponseBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.BookErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.GlobalErrorCode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class BookControllerTest extends ControllerTestSupport {

  /*
  * 도서 등록 관련 테스트
  * */
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
    given(bookService.createBook(any(), any())).willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR));

    // when & then
    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 검증
  }

  @DisplayName("도서 등록 시 썸네일 이미지가 포함되어 있으면 정상적으로 등록된다.")
  @Test
  void createBook_Success_WithImage() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request)
    );

    // 내용이 있는 정상적인 이미지 파일 mocking
    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage", "image.jpg", "image/jpeg", "dummy content".getBytes()
    );

    BookDto responseDto = BookDto.builder()
        .id(UUID.randomUUID())
        .title("자바의 정석")
        .build();
    given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(responseDto);

    // when & then
    mockMvc.perform(
        multipart("/api/books")
            .file(bookData)
            .file(thumbnailImage)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
    )
        .andDo(print())
        .andExpect(status().isCreated()); // 201 검증
  }

  @DisplayName("도서 등록 시 썸네일 이미지가 빈 파일(0 byte)이어도 정상적으로 등록된다.")
  @Test
  void createBook_Success_WithEmptyImage() throws Exception {
    // given
    BookCreateRequest request = BookCreateRequest.builder()
        .title("자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("자바의 정석 기초편")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsBytes(request)
    );

    // 이름은 있지만 내용은 텅 빈(0 byte) 파일 mocking (isEmpty() = true 유도)
    MockMultipartFile emptyImage = new MockMultipartFile(
        "thumbnailImage", "empty.jpg", "image/jpeg", new byte[0]
    );

    BookDto responseDto = BookDto.builder().id(UUID.randomUUID()).title("자바의 정석").build();
    given(bookService.createBook(any(BookCreateRequest.class), any())).willReturn(responseDto);

    // when & then
    mockMvc.perform(
            multipart("/api/books")
                .file(bookData)
                .file(emptyImage) // 빈 파일 전송
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        .andDo(print())
        .andExpect(status().isCreated());
  }

  /*
  * 도서 상세 조회 관련 테스트
  * */
  @DisplayName("도서 상세 조회 API 호출 시 200 응답과 도서 정보를 반환한다.")
  @Test
  void getBook_Success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookDto bookDto = BookDto.builder()
        .id(bookId)
        .title("자바의 정석")
        .author("남궁성")
        .build();

    given(bookService.getBook(bookId)).willReturn(bookDto);

    // when & then
    mockMvc.perform(get("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("자바의 정석"))
        .andExpect(jsonPath("$.author").value("남궁성"));
  }

  @DisplayName("존재하지 않는 도서 ID로 조회하면 404 에러를 반환한다.")
  @Test
  void getBook_Fail_NotFound() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    // 서비스에서 도서를 찾지 못해 예외를 던지는 상황을 mocking
    given(bookService.getBook(bookId)).willThrow(new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

    // when & then
    mockMvc.perform(get("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isNotFound()); // 404 검증
  }

  @DisplayName("도서 상세 조회 중 서버 내부 에러가 밠갱하면 500 에러를 반환한다.")
  @Test
  void getBook_Fail_InternalServerError() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    // 서비스 계층에서 예기치 않은 런타임 에러가 발생하는 상황 mocking
    given(bookService.getBook(bookId))
        .willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "서버 내부 에러 발생"));

    // when & then
    mockMvc.perform(get("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 검증
  }

  /*
  * 도서 수정 관련 테스트
  * */
  @DisplayName("도서 정보 수정 API 호출 시 200 응답과 수정된 도서 정보를 반환한다.")
  @Test
  void updateBook_Success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 자바의 정석")
        .author("홍성휘")
        .publisher("도우출판")
        .description("저자가 홍성휘로 바뀐 개정판입니다.")
        .publishedDate(LocalDate.of(2026, 3, 8))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    MockMultipartFile thumbnailImage = new MockMultipartFile(
        "thumbnailImage",
        "thumbnail.jpg",
        "image/jpeg",
        "dummy content".getBytes()
    );

    BookDto responseDto = BookDto.builder()
        .id(bookId)
        .title("수정된 자바의 정석")
        .build();

    given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any())).willReturn(responseDto);

    // when & then
    mockMvc.perform(
        multipart("/api/books/{bookId}", bookId)
            .file(bookData)
            .file(thumbnailImage)
            // multipart()가 기본적으로 POST 이기 때문에 PATCH로 강제 변경함
            .with(req -> {
              req.setMethod("PATCH");
              return req;
            })
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
    )
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("수정된 자바의 정석"));
  }

  @DisplayName("도서 정보 수정 시 필수 값(제목 등)이 누락되면 400 에러를 반환한다.")
  @Test
  void updateBook_Fail_BadRequest() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("") // 제목 누락 (필수값)
        .author("남궁성")
        .publisher("도우출판")
        .description("내용이 추가된 개정판입니다.")
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
            multipart("/api/books/{bookId}", bookId)
                .file(bookData)
                .with(req -> {
                  req.setMethod("PATCH");
                  return req;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        .andDo(print())
        .andExpect(status().isBadRequest()); // 400 검증
  }

  @DisplayName("존재하지 않는 도서를 수정하려 하면 404 에러를 반환한다.")
  @Test
  void updateBook_Fail_NotFound() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("내용이 추가된 개정판입니다.")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // 서비스에서 찾지 못해 예외 발생
    given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any()))
        .willThrow(new BusinessException(BookErrorCode.BOOK_NOT_FOUND));

    // when & then
    mockMvc.perform(
            multipart("/api/books/{bookId}", bookId)
                .file(bookData)
                .with(req -> {
                  req.setMethod("PATCH");
                  return req;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        .andDo(print())
        .andExpect(status().isNotFound()); // 404 검증
  }

  @DisplayName("도서 정보 수정 시 ISBN이 중복되면 409 에러를 반환한다.")
  @Test
  void updateBook_Fail_Conflict() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("내용이 추가된 개정판입니다.")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // 서비스에서 ISBN 중복 예외 발생
    given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any()))
        .willThrow(new BusinessException(BookErrorCode.DUPLICATE_ISBN));

    // when & then
    mockMvc.perform(
            multipart("/api/books/{bookId}", bookId)
                .file(bookData)
                .with(req -> {
                  req.setMethod("PATCH");
                  return req;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        .andDo(print())
        .andExpect(status().isConflict()); // 409 검증
  }

  @DisplayName("도서 정보 수정 중 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void updateBook_Fail_InternalServerError() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    BookUpdateRequest request = BookUpdateRequest.builder()
        .title("수정된 자바의 정석")
        .author("남궁성")
        .publisher("도우출판")
        .description("내용이 추가된 개정판입니다.")
        .publishedDate(LocalDate.of(2016, 1, 1))
        .build();

    MockMultipartFile bookData = new MockMultipartFile(
        "bookData",
        "",
        MediaType.APPLICATION_JSON_VALUE,
        objectMapper.writeValueAsBytes(request)
    );

    // 예기치 않은 서버 에러 발생
    given(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class), any()))
        .willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR));

    // when & then
    mockMvc.perform(
            multipart("/api/books/{bookId}", bookId)
                .file(bookData)
                .with(req -> {
                  req.setMethod("PATCH");
                  return req;
                })
                .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 상태 코드 검증
  }

  /*
  * 도서 삭제 관련 테스트
  * */
  @DisplayName("도서 논리 삭제 API 호출 시 204 응답을 반환한다.")
  @Test
  void softDeleteBook_Success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isNoContent()); // 204 검증
  }

  @DisplayName("도서 물리 삭제 API 호출 시 204 응답을 반환한다.")
  @Test
  void hardDeleteBook_Success() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
        .andDo(print())
        .andExpect(status().isNoContent()); // 204 검증
  }

  @DisplayName("존재하지 않는 도서를 논리 삭제하려 하면 404 에러를 반환한다.")
  @Test
  void softDeleteBook_Fail_NotFound() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    // void 반환 메서드는 given() X / willThrow().given() 순서 작성.
    willThrow(new BusinessException(BookErrorCode.BOOK_NOT_FOUND))
        .given(bookService).softDeleteBook(bookId);

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isNotFound()); // 404 검증
  }

  @DisplayName("도서 논리 삭제 중 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void softDeleteBook_Fail_InternalServerError() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR))
        .given(bookService).softDeleteBook(bookId);

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}", bookId))
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 검증
  }

  @DisplayName("존재하지 않는 도서를 물리 삭제하려 하면 404 에러를 반환한다.")
  @Test
  void hardDeleteBook_Fail_NotFound() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    willThrow(new BusinessException(BookErrorCode.BOOK_NOT_FOUND))
        .given(bookService).hardDeleteBook(bookId);

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
        .andDo(print())
        .andExpect(status().isNotFound()); // 404 검증
  }

  @DisplayName("도서 물리 삭제 중 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void hardDeleteBook_Fail_InternalServerError() throws Exception {
    // given
    UUID bookId = UUID.randomUUID();
    willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "예기치 않은 서버 에러"))
        .given(bookService).hardDeleteBook(bookId);

    // when & then
    mockMvc.perform(delete("/api/books/{bookId}/hard", bookId))
        .andDo(print())
        .andExpect(status().isInternalServerError()); // 500 검증
  }

  /*
  * 도서 목록 검색 관련 테스트
  * */
  @DisplayName("도서 목록 검색 API 호출 시 파라미터를 잘 매핑해서 200 응답과 목록을 반환한다.")
  @Test
  void searchBooks_Success() throws Exception {
    // given
    BookDto bookDto = BookDto.builder()
        .id(UUID.randomUUID())
        .title("자바의 정석")
        .author("남궁성")
        .build();

    CursorPageResponseBookDto responseBookDto = CursorPageResponseBookDto.builder()
        .content(List.of(bookDto))
        .nextCursor("자바의 정석")
        .nextAfter(Instant.now())
        .size(10)
        .hasNext(false)
        .build();

    // 서비스 메서드 mocking
    given(bookService.searchBooks(any(BookPageRequest.class))).willReturn(responseBookDto);

    // when & then
    mockMvc.perform(get("/api/books")
        .param("keyword", "자바")
        .param("limit", "10")
        .param("orderBy", "title")
        .param("direction", "DESC"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].title").value("자바의 정석"))
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @DisplayName("도서 목록 검색 시 파라미터 타입이 맞지 않으면 400 에러를 반환한다.")
  @Test
  void searchBooks_Fail_BadRequest() throws Exception {
    // when & then
    // limit에 문자열을 넣어서 400 에러 유도
    mockMvc.perform(get("/api/books")
        .param("limit", "number"))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("도서 목록 검색 중 예기치 않은 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void searchBooks_Fail_InternalServerError() throws Exception {
    // given
    given(bookService.searchBooks(any(BookPageRequest.class)))
        .willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "예기치 않은 서버 에러 발생"));

    // when & then
    mockMvc.perform(get("/api/books")
        .param("keyword", "자바"))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }

  /*
   * 인기 도서 목록 조회 관련 테스트
   */
  @DisplayName("인기 도서 목록 조회 API 호출 시 파라미터를 잘 매핑해서 200 응답과 목록을 반환한다.")
  @Test
  void getPopularBooks_Success() throws Exception {
    // given
    CursorPageResponsePopularBookDto responsePopularBookDto = new CursorPageResponsePopularBookDto(
        Collections.emptyList(),
        "1",
        Instant.now(),
        10,
        null,
        false
    );

    given(dashboardService.getPopularBooks(any(DashboardPageRequest.class))).willReturn(responsePopularBookDto);

    // when & then
    mockMvc.perform(get("/api/books/popular")
        .param("period", "WEEKLY")
        .param("limit", "10"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.size").value(10))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @DisplayName("인기 도서 목록 조회 시 파라미터 타입이 맞지 않으면(예: limit에 문자열) 400 에러를 반환한다.")
  @Test
  void getPopularBooks_Fail_BadRequest() throws Exception {
    // when & then
    mockMvc.perform(get("/api/books/popular")
            .param("limit", "number")) // 숫자가 들어가야 할 곳에 문자열을 넣어서 타입 미스매치 유도
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("인기 도서 목록 조회 중 서버 내부 에러가 발생하면 500 에러를 반환한다.")
  @Test
  void getPopularBooks_Fail_InternalServerError() throws Exception {
    // given
    // 서비스에서 예기치 못한 에러가 터지는 상황을 Mocking
    given(dashboardService.getPopularBooks(any(DashboardPageRequest.class)))
        .willThrow(new BusinessException(GlobalErrorCode.INTERNAL_SERVER_ERROR, "예기치 않은 서버 에러 발생"));

    // when & then
    mockMvc.perform(get("/api/books/popular"))
        .andDo(print())
        .andExpect(status().isInternalServerError());
  }
}
