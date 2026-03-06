package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  /**
   * 알림 생성 공통 메서드
   * @param toUser  알림 받을 사람 (리뷰 작성자)
   * @param review  리뷰 예) [알라딘]님이 나의 리뷰를 좋아합니다.
   * @param message 알림 메시지
   */
  @Override
  @Transactional
  public void createNotification(User toUser, Review review, String message) {

    log.info("[알림 생성 시작] message: {}", message);

    Notification notification = Notification.builder()
        .user(toUser)
        .review(review)
        .message(message)
        .reviewContent(review.getContent())
        .build();
    notificationRepository.save(notification);

    log.info("[알림 생성 완료] message: {}", message);
  }
}
