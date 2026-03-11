package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID>, ReviewRepositoryCustom {

    @Query("SELECT r FROM Review r JOIN FETCH r.user JOIN FETCH r.book WHERE r.book.id = :bookId")
    List<Review> findAllByBookId(@Param("bookId") UUID bookId);

    // 비관적 락을 통해 동시성 제어를 해결 - 트랜잭션이 실행 중이면 다른 트랙잭션은 수행할 수 없음
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Review r WHERE r.id = :reviewId")
    Optional<Review> findByIdWithPessimisticLock(@Param("reviewId") UUID reviewId);

    @Query(value = "SELECT * FROM reviews WHERE book_id = :bookId AND user_id = :userId", nativeQuery = true)
    Optional<Review> findByBookIdAndUserIdIncludeDeleted(@Param("bookId") UUID bookId, @Param("userId") UUID userId);

    // @SQLRestriction("deleted_at IS NULL"), JPQL에서 조회할 경우 deleteAt != null 인 경우 조회되지 않기 때문에 네이티브 쿼리 사용
    @Query(value = "SELECT * FROM reviews WHERE id = :id", nativeQuery = true)
    Optional<Review> findByIdIncludeDeleted(@Param("id") UUID id);

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM reviews WHERE id = :id", nativeQuery = true)
    void hardDeleteById(@Param("id") UUID id);

    @Query("SELECT r FROM Review r JOIN FETCH r.book WHERE r.createdAt >= :since")
    List<Review> findAllByCreatedAtAfter(@Param("since") Instant since);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.commentCount = r.commentCount + 1 WHERE r.id = :reviewId")
    void increaseCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.commentCount = r.commentCount - 1 WHERE r.id = :reviewId AND r.commentCount > 0")
    void decreaseCommentCount(@Param("reviewId") UUID reviewId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount + 1 WHERE r.id = :reviewId")
    void increaseLikeCount(@Param("reviewId") UUID reviewId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Review r SET r.likeCount = r.likeCount - 1 WHERE r.id = :reviewId AND r.likeCount > 0")
    void decreaseLikeCount(@Param("reviewId") UUID reviewId);
}
