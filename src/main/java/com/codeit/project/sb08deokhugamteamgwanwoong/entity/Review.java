package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseRemovableEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_book",
                        columnNames = {"user_id", "book_id"}
                )
        }
)
public class Review extends BaseRemovableEntity {

    @Column(
            name = "rating",
            nullable = false
    )
    private Integer rating;

    @Column(
            name = "content",
            columnDefinition = "TEXT",
            nullable = false
    )
    private String content;

    @Column(
            name = "like_count",
            nullable = false
    )
    private Integer likeCount = 0;

    @Column(
            name = "comment_count",
            nullable = false
    )
    private Integer commentCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    @Builder
    public Review(Integer rating, String content, User user, Book book) {
        this.rating = rating;
        this.content = content;
        this.user = user;
        this.book = book;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public void update(Integer newRating, String newContent) {
        if (newRating != null) {
            this.rating = newRating;
        }
        if (newContent != null && !newContent.isBlank()) {
            this.content = newContent;
        }
    }
}
