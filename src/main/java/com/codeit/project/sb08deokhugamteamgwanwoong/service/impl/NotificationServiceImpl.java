package com.codeit.project.sb08deokhugamteamgwanwoong.service.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.CursorPageResponseNotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationDto;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationUpdateRequest;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.BusinessException;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.NotificationErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.exception.enums.UserErrorCode;
import com.codeit.project.sb08deokhugamteamgwanwoong.mapper.NotificationMapper;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  private final UserRepository userRepository;

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
   * @return               NotificationDto
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
   * 모든 알림 읽음 처리
   * @param requestUserId 요청자 Id
   */
  @Override
  @Transactional
  public void allConfirmNotification(UUID requestUserId) {

    log.info("[모든 알림 읽음 처리 시작] requestUserId: {}", requestUserId);

    if(!userRepository.existsById(requestUserId)) {
      log.warn("[유저 조회 실패] 해당 유저가 존재하지 않습니다. requestUserId: {}", requestUserId);
      throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    notificationRepository.allConfirmNotification(requestUserId);

    log.info("[모든 알림 읽음 처리 성공] requestUserId: {}", requestUserId);
  }

  /**
   * 알림 목록 조회
   * @param userId      유저 Id
   * @param direction   정렬 방향
   * @param cursor      마지막 생성 일자
   * @param after       마지막 생성 일자
   * @param limit       조회 개수
   * @return            CursorPageResponseNotificationDto
   */
  @Override
  public CursorPageResponseNotificationDto getNotifications(
      UUID userId, Direction direction,
      Instant cursor, Instant after, int limit) {

    log.info("[알림 목록 조회 시작] userId: {}, limit: {}", userId, limit);

    PageRequest pageable = PageRequest.of(0, limit, Sort.by(direction, "createdAt"));

    // 데이터 조회
    List<Notification> notifications = notificationRepository.findAllNotification(userId, cursor, after, pageable);

    // 전체 개수 조회
    long totalElements = notificationRepository.countByUserId(userId);

    List<NotificationDto> notiList = notifications.stream()
        .map(notificationMapper::toDto)
        .toList();

    // 다음 페이지 여부 확인
    boolean hasNext = notifications.size() > limit;

    List<NotificationDto> content = hasNext ? notiList.subList(0, limit) : notiList;

    log.info("[알림 목록 조회 완료] 현재 조회 개수: {}, hasNext: {}", content.size(), hasNext);

    return CursorPageResponseNotificationDto.of(content, totalElements, limit, hasNext);
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
