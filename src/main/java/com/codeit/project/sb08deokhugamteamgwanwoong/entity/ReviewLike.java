package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "review_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_like_user_review",
                        columnNames = {"userId", "review_id"}
                )
        }
)
public class ReviewLike extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "review_id",
            nullable = false
    )
    private Review review;

    @Builder
    public ReviewLike(Review review, User user) {
        this.review = review;
        this.user = user;
    }
}
