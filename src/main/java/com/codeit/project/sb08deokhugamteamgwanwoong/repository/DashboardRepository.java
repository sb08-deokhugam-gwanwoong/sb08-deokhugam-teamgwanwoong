package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

	/** 인기 도서 배치: SQL에서 점수 계산 + RANK() OVER로 ranking_pos 산출 후 INSERT */
	@Modifying
	@Query(value = """
		INSERT INTO dashboard (id, target_id, target_type, period_type, score, ranking_pos, created_at)
		SELECT gen_random_uuid(), book_id, 'BOOK', :periodType, score, rn, CURRENT_TIMESTAMP
		FROM (
		  SELECT book_id, score, RANK() OVER (ORDER BY score DESC)::int AS rn
		  FROM (
		    SELECT book_id, COUNT(*) * 0.4 + AVG(rating)::double precision * 0.6 AS score
		    FROM reviews
		    WHERE created_at >= :since AND deleted_at IS NULL
		    GROUP BY book_id
		  ) inner_sub
		) outer_sub
		WHERE rn <= 100
		""", nativeQuery = true)
	int insertPopularBooks(@Param("since") Instant since, @Param("periodType") String periodType);

	/** 인기 리뷰 배치: SQL에서 점수 계산 + RANK() OVER로 ranking_pos 산출 후 INSERT */
	@Modifying
	@Query(value = """
		INSERT INTO dashboard (id, target_id, target_type, period_type, score, ranking_pos, created_at)
		SELECT gen_random_uuid(), id, 'REVIEW', :periodType, score, rn, CURRENT_TIMESTAMP
		FROM (
		  SELECT id, score, RANK() OVER (ORDER BY score DESC)::int AS rn
		  FROM (
		    SELECT id, like_count * 0.3 + comment_count * 0.7 AS score
		    FROM reviews
		    WHERE created_at >= :since AND deleted_at IS NULL
		  ) inner_sub
		) outer_sub
		WHERE rn <= 100
		""", nativeQuery = true)
	int insertPopularReviews(@Param("since") Instant since, @Param("periodType") String periodType);

	/** 파워 유저 배치: SQL에서 점수 계산 + RANK() OVER로 ranking_pos 산출 후 INSERT */
	@Modifying
	@Query(value = """
		WITH review_scores AS (
		  SELECT user_id, SUM(like_count * 0.3 + comment_count * 0.7) AS review_score_sum
		  FROM reviews WHERE created_at >= :since AND deleted_at IS NULL
		  GROUP BY user_id
		),
		like_counts AS (
		  SELECT user_id, COUNT(*)::bigint AS cnt FROM review_likes WHERE created_at >= :since GROUP BY user_id
		),
		comment_counts AS (
		  SELECT user_id, COUNT(*)::bigint AS cnt FROM comments WHERE created_at >= :since GROUP BY user_id
		),
		all_users AS (
		  SELECT user_id FROM review_scores
		  UNION SELECT user_id FROM like_counts
		  UNION SELECT user_id FROM comment_counts
		),
		user_scores AS (
		  SELECT u.user_id,
		    COALESCE(r.review_score_sum, 0) * 0.5 + COALESCE(l.cnt, 0) * 0.2 + COALESCE(c.cnt, 0) * 0.3 AS score
		  FROM all_users u
		  LEFT JOIN review_scores r ON u.user_id = r.user_id
		  LEFT JOIN like_counts l ON u.user_id = l.user_id
		  LEFT JOIN comment_counts c ON u.user_id = c.user_id
		)
		INSERT INTO dashboard (id, target_id, target_type, period_type, score, ranking_pos, created_at)
		SELECT gen_random_uuid(), user_id, 'USER', :periodType, score, rn, CURRENT_TIMESTAMP
		FROM (
		  SELECT user_id, score, RANK() OVER (ORDER BY score DESC)::int AS rn
		  FROM user_scores WHERE score > 0
		) sub WHERE rn <= 100
		""", nativeQuery = true)
	int insertPowerUsers(@Param("since") Instant since, @Param("periodType") String periodType);

	/** 최신 랭킹 조회 (오프셋 페이지네이션) */
	@Query("""
	    SELECT d FROM Dashboard d
	    WHERE d.targetType = :targetType 
	      AND d.periodType = :periodType
	    ORDER BY d.createdAt DESC, d.rankingPos ASC
	    """)
	List<Dashboard> findRecentRankings(
			@Param("targetType") DashboardTargetType targetType,
			@Param("periodType") DashboardPeriodEnums periodType,
			Pageable pageable
	);

	/** 커서 페이지네이션 (createdAt DESC 정렬) */
	@Query("""
	    SELECT d FROM Dashboard d
	    WHERE d.targetType = :targetType
	      AND d.periodType = :periodType
	      AND (:after IS NULL OR d.createdAt < :after OR (d.createdAt = :after AND d.rankingPos > :cursorRankingPos))
	    ORDER BY d.createdAt DESC, d.rankingPos ASC
	    """)
	List<Dashboard> findRecentRankingsByCursorDesc(
			@Param("targetType") DashboardTargetType targetType,
			@Param("periodType") DashboardPeriodEnums periodType,
			@Param("after") Instant after,
			@Param("cursorRankingPos") Integer cursorRankingPos,
			Pageable pageable
	);

	/** 커서 페이지네이션 (createdAt ASC 정렬) */
	@Query("""
	    SELECT d FROM Dashboard d
	    WHERE d.targetType = :targetType
	      AND d.periodType = :periodType
	      AND (:after IS NULL OR d.createdAt > :after OR (d.createdAt = :after AND d.rankingPos > :cursorRankingPos))
	    ORDER BY d.createdAt ASC, d.rankingPos ASC
	    """)
	List<Dashboard> findRecentRankingsByCursorAsc(
			@Param("targetType") DashboardTargetType targetType,
			@Param("periodType") DashboardPeriodEnums periodType,
			@Param("after") Instant after,
			@Param("cursorRankingPos") Integer cursorRankingPos,
			Pageable pageable
	);

	@Modifying
	@Query("DELETE FROM Dashboard d WHERE d.targetType = :targetType AND d.periodType = :periodType")
	void deleteByTargetTypeAndPeriodType(
			@Param("targetType") DashboardTargetType targetType,
			@Param("periodType") DashboardPeriodEnums periodType
	);
}
