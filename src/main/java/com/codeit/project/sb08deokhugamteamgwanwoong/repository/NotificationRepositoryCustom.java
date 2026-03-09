package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {

  List<Notification> findAllNotification(
      UUID userId,
      Instant cursor,
      Instant after,
      Pageable pageable
  );
}
