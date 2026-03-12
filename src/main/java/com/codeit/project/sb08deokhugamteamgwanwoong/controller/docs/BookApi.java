package com.codeit.project.sb08deokhugamteamgwanwoong.controller.docs;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookPageRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.CursorPageResponseBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.NaverBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.CursorPageResponsePopularBookDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.dashboard.DashboardPageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "도서 관리", description = "도서 관련 API")
public interface BookApi {

  @Operation(summary = "도서 등록", description = "새로운 도서를 등록합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "도서 등록 성공", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "409", description = "ISBN 중복", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  ResponseEntity<BookDto> createBook(
      @Parameter(description = "도서 등록 정보 (JSON)") @RequestPart("bookData") BookCreateRequest request,
      @Parameter(description = "도서 썸네일 이미지 파일") @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "도서 상세 정보 조회", description = "도서 ID로 상세 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  ResponseEntity<BookDto> getBook(
      @Parameter(description = "조회할 도서의 UUID", required = true) @PathVariable("bookId") UUID bookId
  );

  @Operation(summary = "도서 정보 수정", description = "도서 정보를 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 정보 수정 성공", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패, ISBN 형식 오류 등)", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "409", description = "ISBN 중복", content = @Content(schema = @Schema(implementation = BookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = BookDto.class)))
  })
  ResponseEntity<BookDto> updateBook(
      @Parameter(description = "수정할 도서의 UUID", required = true) @PathVariable("bookId") UUID bookId,
      @Parameter(description = "수정할 도서 정보 (JSON)") @RequestPart(value = "bookData", required = false) BookUpdateRequest request,
      @Parameter(description = "수정할 도서 썸네일 이미지 파일") @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
  );

  @Operation(summary = "도서 논리 삭제", description = "도서를 논리적으로 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> softDeleteBook(
      @Parameter(description = "삭제할 도서의 UUID", required = true) @PathVariable("bookId") UUID bookId
  );

  @Operation(summary = "도서 물리 삭제", description = "도서를 물리적으로 삭제합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "도서 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  ResponseEntity<Void> hardDeleteBook(
      @Parameter(description = "물리 삭제할 도서의 UUID", required = true) @PathVariable("bookId") UUID bookId
  );

  @Operation(summary = "도서 목록 조회", description = "검색 조건에 맞는 도서 목록을 커서 기반으로 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 목록 조회 성공", content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 파라미터 타입 오류 등)", content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = CursorPageResponseBookDto.class)))
  })
  ResponseEntity<CursorPageResponseBookDto> searchBooks(
      @ParameterObject BookPageRequest request
  );

  @Operation(summary = "인기 도서 목록 조회", description = "기간별 인기 도서 목록을 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "인기 도서 목록 조회 성공", content = @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (랭킹 기간 오류, 정렬 방향 오류 등)", content = @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = CursorPageResponsePopularBookDto.class)))
  })
  ResponseEntity<CursorPageResponsePopularBookDto> getPopularBooks(
      @ParameterObject DashboardPageRequest request
  );

  @Operation(summary = "ISBN으로 도서 정보 조회", description = "Naver API를 통해 ISBN으로 도서 정보를 조회합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "도서 정보 조회 성공", content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 ISBN 형식", content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
      @ApiResponse(responseCode = "404", description = "도서 정보 없음", content = @Content(schema = @Schema(implementation = NaverBookDto.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = NaverBookDto.class)))
  })
  ResponseEntity<NaverBookDto> getBookInfoByIsbn(
      @Parameter(description = "ISBN 번호", required = true, example = "9788965402602") @RequestParam("isbn") String isbn
  );

  @Operation(summary = "OCR 기반 ISBN 인식", description = "OCR을 통해 도서 뒷면의 바코드 이미지에서 ISBN을 인식하여 추출합니다.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "ISBN 인식 성공", content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 이미지 형식, 파일 누락 또는 OCR 인식 실패", content = @Content(schema = @Schema(implementation = String.class))),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류", content = @Content(schema = @Schema(implementation = String.class)))
  })
  ResponseEntity<String> getBookInfoByImage(
      @Parameter(description = "도서 바코드 이미지 파일", required = true) @RequestParam("image") MultipartFile image
  );
}
