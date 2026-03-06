package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "review_id", nullable = false)
  private Review review;

  @Column(nullable = false, length = 100)
  private String message;

  @Column(name = "review_content", nullable = false, columnDefinition = "TEXT")
  private String reviewContent;

  @Column(name = "is_confirmed", nullable = false)
  private boolean isConfirmed = false;

  @Builder
  public Notification(User user, Review review, String message, String reviewContent) {
    this.user = user;
    this.review = review;
    this.message = message;
    this.reviewContent = reviewContent;
  }

  public void confirm() {
    this.isConfirmed = true;
  }
}
