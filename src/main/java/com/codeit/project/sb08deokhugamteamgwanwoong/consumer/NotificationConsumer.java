package com.codeit.project.sb08deokhugamteamgwanwoong.consumer;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.ReviewErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final ReviewRepository reviewRepository;

  @KafkaListener(topics = "notification-topic", groupId = "notification-group")
  public void consume(NotificationEvent event) {

    log.info("[Kafka Consumer] 메시지 수신: {}", event.message());

    try {
      User toUser = userRepository.findById(event.toUserId())
          .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

      Review review = reviewRepository.findById(event.reviewId())
          .orElseThrow(() -> new BusinessException(ReviewErrorCode.REVIEW_NOT_FOUND));

      // 기존 알림 생성 로직 사용
      notificationService.createNotification(toUser, review, event.message());
    } catch (Exception e) {
      log.error("[Kafka Consumer] 알림 생성 중 오류 발생: {}", e.getMessage());
    }
  }
}
