package com.codeit.project.sb08deokhugamteamgwanwoong.entity;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.base.BaseEntity;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardPeriodEnums;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.enums.DashboardTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "dashboard")
public class Dashboard extends BaseEntity {

		@Column(name = "target_id", nullable = false)
		private UUID targetId;

		@Enumerated(EnumType.STRING)
		@Column(name = "target_type", nullable = false)
		private DashboardTargetType targetType;

		@Enumerated(EnumType.STRING)
		@Column(name = "period_type", nullable = false)
		private DashboardPeriodEnums periodType;

		@Column(nullable = false)
		private Double score; // 계산된 점수

		@Column(name = "ranking_pos", nullable = false)
		private Integer rankingPos; // Window Function RANK() 결과

		@Builder
		public Dashboard(UUID targetId, DashboardTargetType targetType, DashboardPeriodEnums periodType, Double score, Integer rankingPos) {
				this.targetId = targetId;
				this.targetType = targetType;
				this.periodType = periodType;
				this.score = score;
				this.rankingPos = rankingPos;
		}
}