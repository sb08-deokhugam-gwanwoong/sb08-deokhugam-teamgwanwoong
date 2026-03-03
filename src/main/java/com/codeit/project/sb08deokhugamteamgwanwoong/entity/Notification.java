package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

  @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "review_id", nullable = false)
  private String reviewId;

  @Column(name = "review_title", nullable = false)
  private String reviewTitle;

  @Column(nullable = false)
  private String content;

  @Column(name = "is_confirmed", nullable = false)
  private boolean isConfirmed = false;

  @Builder
  public Notification(User user, String reviewId, String reviewTitle, String content) {
    this.user = user;
    this.reviewId = reviewId;
    this.reviewTitle = reviewTitle;
    this.content = content;
  }

  public void confirm() {
    this.isConfirmed = true;
  }

}
