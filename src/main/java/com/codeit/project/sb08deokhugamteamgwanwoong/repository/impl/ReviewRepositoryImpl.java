package com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Review;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QBook.book;
import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QReview.review;
import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Review> findAllByCursor(ReviewSearchCondition condition, Pageable pageable) {
        return queryFactory
                .selectFrom(review)
                // N+1 문제를 해결하기 위해 fetchJoin
                .leftJoin(review.user, user).fetchJoin()
                .leftJoin(review.book, book).fetchJoin()
                .where(
                        // 1. 완전 일치 조건 (유저 ID, 책 ID)
                        userIdEq(condition.userId()),
                        bookIdEq(condition.bookId()),
                        // 부분 일치 검색 (키워드)
                        keywordContains(condition.keyword()),
                        // 커서 페이징 조건
                        cursorCondition(condition.cursor(), condition.after(), condition.orderBy(), condition.direction())
                )
                // 동적 정렬
                .orderBy(createOrderSpecifier(condition.orderBy(), condition.direction()))
                .limit(pageable.getPageSize())
                .fetch();
    }

    // 완전 일치 조건
    private BooleanExpression userIdEq(UUID userId) {
        return userId != null ? review.user.id.eq(userId) : null;
    }

    // 완전 일치 조건
    private BooleanExpression bookIdEq(UUID bookId) {
        return bookId != null ? review.book.id.eq(bookId) : null;
    }

    // 부분 일치 조건
    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        // keyword에 유저 닉네임, 책 이름, 리뷰 내용 중 하나라도 있으면 만족(containsIgnoreCase: 부분 일치, 대소문자 무시)
        return review.user.nickname.containsIgnoreCase(keyword)
                .or(review.book.title.containsIgnoreCase(keyword))
                .or(review.content.containsIgnoreCase(keyword));
    }

    // 다음 페이지를 위한 커서 기준점 필터링
    private BooleanExpression cursorCondition(String cursor, Instant after, String orderBy, Sort.Direction direction) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        // 평점 순 정렬
        if ("rating".equals(orderBy)) {
            int ratingCursor = Integer.parseInt(cursor); // String to int

            if (direction == Sort.Direction.ASC) {
                // 평점이 더 높거나 또는 평점이 같을 때 작성일이 최근인 것
                return review.rating.gt(ratingCursor)
                        .or(review.rating.eq(ratingCursor).and(after != null ? review.createdAt.gt(after) : null));
            } else {
                // 평점이 더 낮거나 또는 평점이 같은데 작성일이 오래된 것
                return review.rating.lt(ratingCursor)
                        .or(review.rating.eq(ratingCursor).and(after != null ? review.createdAt.lt(after) : null));
            }
        } else {
            // orderBy == "createdAt"인 경우 (cursor가 날짜 형태)
            Instant timeCursor = Instant.parse(cursor);
            if (direction == Sort.Direction.ASC) {
                return review.createdAt.gt(timeCursor);
            } else {
                return review.createdAt.lt(timeCursor);
            }
        }
    }

    // 정렬 기준
    private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, Sort.Direction direction) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
        Order targetOrder = direction.isAscending() ? Order.ASC : Order.DESC;

        // 정렬 조건 우선 순위
        if ("rating".equals(orderBy)) {
            // 1순위 - 평점
            orderSpecifiers.add(new OrderSpecifier<>(targetOrder, review.rating));
            // 2순위 - 생성 시간(보조)
            orderSpecifiers.add(new OrderSpecifier<>(targetOrder, review.createdAt));
        } else {
            orderSpecifiers.add(new OrderSpecifier<>(targetOrder, review.createdAt));
        }

        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}
