package com.codeit.project.sb08deokhugamteamgwanwoong.repository;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
