package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  @Modifying(clearAutomatically = true)
  @Query("""
      UPDATE Notification n SET n.isConfirmed = true
      WHERE n.user.id = :userId AND n.isConfirmed = false
      """)
  void allConfirmNotification(@Param("userId") UUID userId);
}
