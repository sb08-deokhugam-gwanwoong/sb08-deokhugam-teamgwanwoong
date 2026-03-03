package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseRemovableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseRemovableEntity {

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "review_id", nullable = false)
  private Long reviewId;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Builder
  private Comment(Long userId, Long reviewId, String content) {
    this.userId = userId;
    this.reviewId = reviewId;
    this.content = content;

  }

  public void updateContent(String content) {
    this.content = content;
  }
}
