package com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl;

import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QNotification.notification;
import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QReview.review;

import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Notification;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.NotificationRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findAllNotification(UUID userId, Instant cursor, Instant after, Pageable pageable) {

    // 정렬 방향 추출 (기본값: DESC)
    Direction direction = pageable.getSort().stream()
        .map(Sort.Order::getDirection)
        .findFirst()
        .orElse(Direction.DESC);

    // 정렬 타입 결정
    Order queryOrder = direction.isAscending() ? Order.ASC : Order.DESC;

    return queryFactory
        .selectFrom(notification)
        .leftJoin(notification.review, review).fetchJoin()
        .where(
            notification.user.id.eq(userId),
            cursorCondition(cursor, after, direction) // 복합적인 커서 조건
        )
        .orderBy(
            new OrderSpecifier<>(queryOrder, notification.createdAt)
        )
        .limit(pageable.getPageSize() + 1)            // 다음 페이지 여부 확인용
        .fetch();
  }

  /**
   * 복합 커서 조건으로 데이터 추출
   * @param cursor    알림 생성 일자
   * @param after     알림 생성 일자
   * @param direction 정렬 방향
   * @return BooleanExpression
   */
  private BooleanExpression cursorCondition(Instant cursor, Instant after, Direction direction) {

    if (after == null || cursor == null) {
      return null;
    }

    if (direction.isDescending()) {
      // DESC: (생성일 < after)
      return notification.createdAt.lt(cursor);
    } else {
      // ASC: (생성일 > after)
      return notification.createdAt.gt(cursor);
    }
  }
}
