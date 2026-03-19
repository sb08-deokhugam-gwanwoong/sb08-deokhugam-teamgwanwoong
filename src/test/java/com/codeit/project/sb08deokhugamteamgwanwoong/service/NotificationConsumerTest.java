package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.notification.NotificationEvent;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.User;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.UserRepository;
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.NotificationConsumer;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerTest {

  @Mock
  private NotificationService notificationService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private NotificationConsumer notificationConsumer;

  @Test
  @DisplayName("카프카 메시지 수신 시 알림 생성 로직이 정상 호출되어야 한다.")
  void consume_Success_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    NotificationEvent event = new NotificationEvent(userId, reviewId, "테스트 메시지");

    User user = mock(User.class);
    Review review = mock(Review.class);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

    // When - 컨슈머 메서드를 직접 호출
    notificationConsumer.consume(event);

    verify(notificationService).createNotification(user, review, "테스트 메시지");
    verify(userRepository).findById(userId);
    verify(reviewRepository).findById(reviewId);
  }

  @Test
  @DisplayName("카프카 메시지 수신 실패: 존재하지 않는 유저일 경우 catch 블록에서 처리되어야 한다.")
  void consume_UserNotFound_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    NotificationEvent event = new NotificationEvent(userId, reviewId, "테스트 메시지");

    // 유저를 찾지 못하도록 설정
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // When
    notificationConsumer.consume(event);

    // Then
    // 서비스 호출이 일어나지 않았는지 검증
    verify(notificationService, never()).createNotification(any(), any(), any());
  }

  @Test
  @DisplayName("카프카 메시지 수신 실패: 존재하지 않는 리뷰일 경우 catch 블록에서 처리되어야 한다.")
  void consume_ReviewNotFound_Fail_Test() {
    // Given
    UUID userId = UUID.randomUUID();
    UUID reviewId = UUID.randomUUID();
    NotificationEvent event = new NotificationEvent(userId, reviewId, "테스트 메시지");

    User user = mock(User.class);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    // 리뷰를 찾지 못하도록 설정
    when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

    // When
    notificationConsumer.consume(event);

    // Then
    verify(notificationService, never()).createNotification(any(), any(), any());
  }
}
