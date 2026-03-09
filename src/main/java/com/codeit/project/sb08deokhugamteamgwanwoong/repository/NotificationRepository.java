package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  @Modifying(clearAutomatically = true)
  @Query("""
      UPDATE Notification n SET n.isConfirmed = true
      WHERE n.user.id = :userId AND n.isConfirmed = false
      """)
  void allConfirmNotification(@Param("userId") UUID userId);

  // 특정 유저의 전체 알림 개수 조회 (totalElements 전용)
  long countByUserId(UUID userId);

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Notification n WHERE n.isConfirmed = true AND n.createdAt < :limitDate")
  void deleteOldConfirmedNotifications(@Param("limitDate") Instant limitDate);
}
