package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Dashboard;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

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
}
