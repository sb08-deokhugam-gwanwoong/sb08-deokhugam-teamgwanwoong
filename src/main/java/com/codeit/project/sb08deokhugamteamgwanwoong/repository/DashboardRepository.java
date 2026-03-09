package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

	/** 최신 랭킹 조회 (오프셋 페이지네이션) */
	@Query("""
	    SELECT d FROM Dashboard d
	    WHERE d.targetType = :targetType 
	      AND d.periodType = :periodType
	    ORDER BY d.createdAt DESC, d.rankingPos ASC
	    """)
	List<Dashboard> findRecentRankings(
			@Param("targetType") String targetType,
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
			@Param("targetType") String targetType,
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
			@Param("targetType") String targetType,
			@Param("periodType") DashboardPeriodEnums periodType,
			@Param("after") Instant after,
			@Param("cursorRankingPos") Integer cursorRankingPos,
			Pageable pageable
	);
}
