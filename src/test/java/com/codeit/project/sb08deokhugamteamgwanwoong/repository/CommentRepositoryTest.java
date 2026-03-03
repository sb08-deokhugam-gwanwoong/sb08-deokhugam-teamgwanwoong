package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class CommentRepositoryTest extends RepositoryTestSupport {

  @Autowired
  private CommentRepository commentRepository;

  @Test
  @DisplayName("댓글이 정상적으로 등록되어야 한다")
  void saveCommentTest() {

    //given
    Comment comment = Comment.builder()
        .userId(1L)
        .reviewId(1L)
        .content("테스트 comment 입니다")
        .build();

    //when
    Comment savedComment = commentRepository.save(comment);

    //then
    assertThat(savedComment.getId()).isNotNull();
    assertThat(savedComment.getContent()).isEqualTo("테스트 comment 입니다");
  }
}
