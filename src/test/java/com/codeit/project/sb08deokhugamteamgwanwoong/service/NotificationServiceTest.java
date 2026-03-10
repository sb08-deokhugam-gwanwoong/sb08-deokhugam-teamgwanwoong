package com.codeit.project.sb08deokhugamteamgwanwoong.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
import com.codeit.project.sb08deokhugamteamgwanwoong.service.impl.NotificationServiceImpl;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  @Nested
  @DisplayName("알림 생성 및 수정 로직 검증")
  class CreateAndUpdate {

    @Test
    @DisplayName("알림 생성 성공: 알림을 생성하고, 저장소에 저장되어야 한다.")
    void createNotification_Test() {
      // Given
      User user = mock(User.class);
      Review review = mock(Review.class);
      String message = "[Tester]님이 나의 리뷰를 좋아합니다.";

      given(review.getContent()).willReturn("테스트 리뷰 내용");

      // When
      notificationService.createNotification(user, review, message);

      // Then
      ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

      verify(notificationRepository).save(captor.capture());

      Notification savedNotification = captor.getValue();

      assertThat(savedNotification.getUser()).isEqualTo(user);
      assertThat(savedNotification.getMessage()).isEqualTo(message);
      assertThat(savedNotification.getReviewContent()).isEqualTo(review.getContent());
    }

    @Test
    @DisplayName("알림 읽음 처리 성공: 본인의 알림인 경우 정상적으로 읽음 처리되고, DTO를 반환해야 한다.")
    void updateNotification_Test() {
      // Given
      UUID notificationId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      NotificationUpdateRequest request = new NotificationUpdateRequest(true);

      Notification notification = mock(Notification.class);
      User user = mock(User.class);

      given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
      given(notification.getUser()).willReturn(user);
      given(user.getId()).willReturn(userId);   // 내 ID와 일치시켜서 권한 체크를 통과시킨다.
      given(notificationMapper.toDto(notification)).willReturn(mock(NotificationDto.class));

      // When
      notificationService.update(notificationId, userId, request);

      // Then
      verify(notification).confirm(true);
      verify(notificationMapper).toDto(notification);
    }

    @Test
    @DisplayName("알림 읽음 처리 실패: 본인의 알림이 아닌 경우 수정 권한 예외가 발생해야 한다.")
    void updateNotification_Fail_Test() {
      // Given
      UUID notificationId = UUID.randomUUID();
      UUID myId = UUID.randomUUID();
      UUID otherId = UUID.randomUUID();

      Notification notification = mock(Notification.class);
      User otherUser = mock(User.class);

      given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));
      given(notification.getUser()).willReturn(otherUser);
      given(otherUser.getId()).willReturn(otherId); // 내 ID와 다름

      // When & Then
      assertThatThrownBy(() -> notificationService.update(notificationId, myId,
          new NotificationUpdateRequest(true)))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException businessException = (BusinessException) ex;
            assertThat(businessException.getErrorCode().getDomain()).isEqualTo("NOTIFICATION");
            assertThat(businessException.getErrorCode().getCode()).isEqualTo("NOTIFICATION-FORBIDDEN");
          });
    }

    @Test
    @DisplayName("알림 읽음 처리 실패: 알림이 존재하지 않는 경우 NOT_FOUND 예외가 발생해야 한다.")
    void updateNotification_NotFound_Fail_Test() {
      // Given
      UUID notificationId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> notificationService.update(notificationId, userId,
          new NotificationUpdateRequest(true)))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException businessException = (BusinessException) ex;
            assertThat(businessException.getErrorCode().getDomain()).isEqualTo("NOTIFICATION");
            assertThat(businessException.getErrorCode().getCode()).isEqualTo("NOTIFICATION-NOT_FOUND");
          })
          .hasMessage(NotificationErrorCode.NOTIFICATION_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 성공: 존재하는 유저의 경우 일괄 업데이트 되어야 한다.")
    void allConfirmNotification_Test() {
      // Given
      UUID userId = UUID.randomUUID();

      given(userRepository.existsById(userId)).willReturn(true);  // 유저 존재 O

      // When
      notificationService.allConfirmNotification(userId);

      // Then
      verify(notificationRepository).allConfirmNotification(userId);
    }

    @Test
    @DisplayName("모든 알림 읽음 처리 실패: 모든 알림 읽음 처리 할 때 유저가 존재하지 않으면 예외가 발생해야 한다.")
    void allConfirmNotification_Fail_Test() {
      // Given
      UUID userId = UUID.randomUUID();

      given(userRepository.existsById(userId)).willReturn(false);  // 유저 존재 X

      // When & Then
      assertThatThrownBy(() -> notificationService.allConfirmNotification(userId))
          .isInstanceOf(BusinessException.class)
          .satisfies(ex -> {
            BusinessException businessException = (BusinessException) ex;
            assertThat(businessException.getErrorCode().getDomain()).isEqualTo("USER");
            assertThat(businessException.getErrorCode().getCode()).isEqualTo("USER-NOT_FOUND");
          })
          .hasMessage(UserErrorCode.USER_NOT_FOUND.getMessage());
    }
  }

  @Nested
  @DisplayName("알림 조회 및 삭제 로직 검증")
  class GetAndDelete {

    @Test
    @DisplayName("알림 조회 성공: 목록 조회 시 limit 보다 데이터가 많으면 hasNext는 true가 나와야 한다.")
    void getNotifications_Test() {
      // Given
      UUID userId = UUID.randomUUID();
      int limit = 1;
      Notification notification1 = mock(Notification.class);
      Notification notification2 = mock(Notification.class);

      given(notificationRepository.findAllNotification(any(), any(), any(), any()))
          .willReturn(Arrays.asList(notification1, notification2));
      given(notificationRepository.countByUserId(userId)).willReturn(10L);
      given(notificationMapper.toDto(any())).willReturn(mock(NotificationDto.class));

      // When
      CursorPageResponseNotificationDto response = notificationService.getNotifications(userId,
          Direction.DESC, null, null, limit);

      // Then
      assertThat(response.hasNext()).isTrue();
      assertThat(response.content()).hasSize(1); // subList에 의해 limit개만 반환됨
    }

    @Test
    @DisplayName("알림 삭제 성공: 확인한 알림 중에 7일이 지난 알림 삭제 시 삭제 메서드가 제대로 호출 되어야 한다.")
    void deleteOldNotifications_Test() {
      // When
      notificationService.deleteOldNotifications();

      // Then
      verify(notificationRepository).deleteOldConfirmedNotifications(any(Instant.class));
    }
  }
}
