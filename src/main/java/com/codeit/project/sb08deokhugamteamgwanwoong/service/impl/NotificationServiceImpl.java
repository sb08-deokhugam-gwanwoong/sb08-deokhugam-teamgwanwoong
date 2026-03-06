package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.NotificationErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.NotificationMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  private final NotificationMapper notificationMapper;

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

  /**
   * 알림 읽음 상태 업데이트
   * @param notificationId 알림 Id
   * @param requestUserId  요청자 Id
   * @param request        알림 업데이트 request
   * @return
   */
  @Override
  @Transactional
  public NotificationDto update(UUID notificationId, UUID requestUserId, NotificationUpdateRequest request) {

    log.info("[알림 읽음 상태 업데이트 시작] notificationId: {}", notificationId);

    Notification notification = findNotificationById(notificationId);

    // 리뷰를 작성한 유저가 아닌 경우
    if (!notification.getUser().getId().equals(requestUserId)) {
      log.warn("[알림 읽음 상태 업데이트 실패] 해당 알림을 수정할 권한이 존재하지 않습니다. requestUserId: {}", requestUserId);
      throw new BusinessException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
    }

    notification.confirm(request.confirmed());

    log.info("[알림 읽음 상태 업데이트 성공] notificationId: {}, isConfirmed: {}", notificationId, notification.isConfirmed());

    return notificationMapper.toDto(notification);
  }

  /**
   * 알림 조회 (공통 메서드)
   * @param notificationId 유저 Id
   * @return Notification
   */
  private Notification findNotificationById(UUID notificationId) {

    return notificationRepository.findById(notificationId)
        .orElseThrow(() -> {
          log.warn("[알림 읽음 상태 업데이트 실패] 알림이 존재하지 않습니다. notificationId: {}", notificationId);
          return new BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        });
  }
}
