package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
    // @Query 애노테이션은 기본적으로 SELECT 쿼리임으로 @Modifying 애노테이션을 추가하여 조회가 아님을 명시
    // 메모리에 올려두지 않고 DB에 직접 쿼리 - 데이터 불일치 막음
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Comment c SET c.deletedAt = :now WHERE c.review.id = :reviewId AND c.deletedAt IS NULL")
    void softDeleteAllByReviewId(@Param("reviewId") UUID reviewId, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Comment c WHERE c.review.id = :reviewId")
    void hardDeleteAllByReviewId(@Param("reviewId") UUID reviewId);

  List<Comment> findAllByReviewId(UUID reviewId);

  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.deletedAt IS NULL AND c.createdAt >= :since")
  List<Comment> findAllByCreatedAtAfter(@Param("since") Instant since);

}
