package com.codeit.project.sb08deokhugamteamgwanwoong.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeit.project.sb08deokhugamteamgwanwoong.controller.support.ControllerTestSupport;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentCreateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CursorPageResponseCommentDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.CommentErrorCode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class CommentControllerTest extends ControllerTestSupport {

  private UUID reviewId;
  private UUID userId;
  private UUID commentId;
  private CommentDto sampleCommentDto;

  @BeforeEach
  void setUp() {
    reviewId = UUID.randomUUID();
    userId = UUID.randomUUID();
    commentId = UUID.randomUUID();

    sampleCommentDto = new CommentDto(
        commentId,
        reviewId,
        userId,
        "웅제",
        "테스트 댓글입니다",
        Instant.now(),
        Instant.now()
    );
  }

  @Nested
  @DisplayName("댓글 등록 API")
  class CreateComment {

    @Test
    @DisplayName("성공 : 댓글을 생성하고 201 Created를 반환한다 ")
    void createComment_Success() throws Exception {
      CommentCreateRequest request = new CommentCreateRequest(reviewId, userId, "테스트 댓글입니다");
      given(commentService.create(any(CommentCreateRequest.class))).willReturn(sampleCommentDto);

      mockMvc.perform(post("/api/comments")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(commentId.toString()))
          .andExpect(jsonPath("$.content").value("테스트 댓글입니다"));

      then(commentService).should(times(1)).create(any(CommentCreateRequest.class));
    }

    @Test
    @DisplayName("실패 : 댓글 내용이 비어있으면 400 Bad Request를 반환한다")
    void createComment_Fail_InvalidInput() throws Exception {
      //given
      CommentCreateRequest badRequest = new CommentCreateRequest(reviewId, userId, "");

      //when & then
      mockMvc.perform(post("/api/comments")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(badRequest)))
          .andDo(print())
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("GLOBAL-INVALID_INPUT"));
    }
  }

  @Nested
  @DisplayName("댓글 목록 조회 API")
  class FindAllComments {

    @Test
    @DisplayName("성공 : 댓글 목록 페이징 조회하고 200 OK를 반환한다")
    void findAllComments_Success() throws Exception {
      //given
      CursorPageResponseCommentDto responseDto = new CursorPageResponseCommentDto(
          List.of(sampleCommentDto),
          commentId.toString(),
          Instant.now(),
          10,
          1L,
          false
      );

      given(commentService.findAllComments(eq(reviewId), any(), any(), eq(10)))
          .willReturn(responseDto);

      //when & then
      mockMvc.perform(get("/api/comments")
              .param("reviewId", reviewId.toString())
              .param("limit", "10"))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content[0].id").value(commentId.toString()))
          .andExpect(jsonPath("$.hasNext").value(false));

      //검증
      then(commentService).should(times(1)).findAllComments(eq(reviewId), any(), any(), eq(10));
    }

    @Test
    @DisplayName("성공 : 댓글 상세 조회하고 200 OK를 반환한다")
    void findById_Success() throws Exception {
      given(commentService.findById(commentId)).willReturn(sampleCommentDto);

      mockMvc.perform(get("/api/comments/{commentId}", commentId))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("테스트 댓글입니다"));

      then(commentService).should(times(1)).findById(commentId);
    }

    @Test
    @DisplayName("실패 : 존재하지 않는 댓글을 조회할 경우 404 Not Found를 반환한다")
    void findById_Fail_NotFound() throws Exception {
      //given
      given(commentService.findById(commentId))
          .willThrow(new BusinessException(CommentErrorCode.COMMENT_NOT_FOUND));

      //when & then
      mockMvc.perform(get("/api/comments/{commentId}", commentId))
          .andDo(print())
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.code").value("COMMENT-NOT_FOUND"))
          .andExpect(jsonPath("$.message").value("해당 댓글이 존재하지 않습니다."));
    }
  }

  @Nested
  @DisplayName("댓글 삭제 API")
  class DeleteComment {

    @Test
    @DisplayName("댓글 논리 삭제 성공 : 204 No Content를 반환한다")
    void softDeleteComment_Success() throws Exception {
      willDoNothing().given(commentService).softDelete(commentId, userId);

      mockMvc.perform(delete("/api/comments/{commentId}", commentId)
              .header("Deokhugam-Request-User-ID", userId.toString()))
          .andDo(print())
          .andExpect(status().isNoContent());

      then(commentService).should(times(1)).softDelete(commentId, userId);
    }

    @Test
    @DisplayName("댓글 물리 삭제 성공 : 204 No Content를 반환한다")
    void hardDeleteComment_Success() throws Exception {
      willDoNothing().given(commentService).hardDelete(commentId, userId);

      mockMvc.perform(delete("/api/comments/{commentId}/hard", commentId)
              .header("Deokhugam-Request-User-ID", userId.toString()))
          .andDo(print())
          .andExpect(status().isNoContent());

      then(commentService).should(times(1)).hardDelete(commentId, userId);
    }

    @Test
    @DisplayName("댓글 삭제 실패 : 본인 댓글이 아닌 댓글을 삭제할 경우 403 Forbidden를 반환한다")
    void deleteComment_Fail_NotOwner() throws Exception {
      //given
      willThrow(new BusinessException(CommentErrorCode.COMMENT_DELETE_DENIED))
          .given(commentService).softDelete(commentId, userId);

      //when & then
      mockMvc.perform(delete("/api/comments/{commentId}", commentId)
              .header("Deokhugam-Request-User-ID", userId.toString()))
          .andDo(print())
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("COMMENT-FORBIDDEN"))
          .andExpect(jsonPath("$.message").value("본인의 댓글만 삭제할 수 있습니다."));
    }
  }

  @Nested
  @DisplayName("댓글 수정 API")
  class UpdateComment {

    @Test
    @DisplayName("댓글 수정 성공 : 200 OK를 반환한다")
    void updateComment_Success() throws Exception {
      CommentUpdateRequest request = new CommentUpdateRequest("수정된 댓글입니다");
      CommentDto updatedDto = new CommentDto(
          commentId, reviewId, userId, "웅제", "수정된 댓글입니다", Instant.now(), Instant.now()
      );

      given(commentService.update(eq(commentId), eq(userId), any(CommentUpdateRequest.class)))
          .willReturn(updatedDto);

      mockMvc.perform(patch("/api/comments/{commentId}", commentId)
              .header("Deokhugam-Request-User-ID", userId.toString())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content").value("수정된 댓글입니다"));

      then(commentService).should(times(1)).update(eq(commentId), eq(userId), any(CommentUpdateRequest.class));
    }
  }
}