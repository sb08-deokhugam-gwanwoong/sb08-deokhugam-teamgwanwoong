package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    Optional<ReviewLike> findByReviewIdAndUserId(UUID reviewId, UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    void hardDeleteAllByReviewId(@Param("reviewId") UUID reviewId);

    @Query("SELECT rl.review.id FROM ReviewLike rl where rl.user.id = :userId AND rl.review.id IN :reviewIds")
    Set<UUID> findLikedReviewIds(@Param("userId") UUID userId, @Param("reviewIds") List<UUID> reviewIds);
}
