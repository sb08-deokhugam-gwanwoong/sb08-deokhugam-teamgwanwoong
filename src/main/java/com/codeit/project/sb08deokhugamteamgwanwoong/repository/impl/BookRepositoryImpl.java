package com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl;

import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QBook.book;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Book;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.BookRepositoryCustom;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookRepositoryImpl implements BookRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Book> findAllByCursor(BookSearchCondition condition, Pageable pageable) {
    return queryFactory
        .selectFrom(book)
        .where(
            // 1. 키워드 검색 (제목, 저자, ISBN)
            keywordContains(condition.keyword()),
            // 2. 커서 페이징 조건
            cursorCondition(condition.cursor(), condition.after(), condition.orderBy(), condition.direction())
        )
        // 3. 동적 정렬 기준
        .orderBy(createOrderSpecifier(condition.orderBy(), condition.direction()))
        .limit(pageable.getPageSize()) // 다음 페이지 유무를 위해 limit + 1개 조회
        .fetch();
  }

  // 부분 일치 검색 조건 (제목 || 저자 || ISBN)
  private BooleanExpression keywordContains(String keyword) {
    if (keyword == null || keyword.isBlank()) {
      return null;
    }
    // 대소문자 구분 없이 제목, 저자, ISBN 중 하나라도 부분 일치하면 찾아냄
    return book.title.containsIgnoreCase(keyword)
        .or(book.author.containsIgnoreCase(keyword))
        .or(book.isbn.containsIgnoreCase(keyword));
  }

  /**
   * orderBy : 사용자가 무엇을 기준으로 정렬할 지 결정
   * direction : 정렬 방향 (ASC, DESC)
   * cursor : 1순위 정렬의 마지막 값
   * after : 2순위 정렬 기준(생성 시간)의 마지막 값 (동점자 구별용 고유 식별자)
   * eq : equal (=) 같다
   * gt : greater than (>) 크다
   * lt : less than (<) 작다
   * goe : (>=) 크거나 같다
   * loe : (<=) 작거나 같다
   * */
  // 커서 기준점 필터링 로직
  /* BooleanExpression : True 또는 False를 뱉어내는 표현식
  * WHERE 절 안에 들어가는 조건들 (title = '자바', rating > 4.5)은 모두 참/거짓으로 판별됨.
  * QueryDSL에서는 이런 WHERE 조건 하나하나를 BooleanExpression이라는 타입 객체로 만듬.
  * 이 객체들을 .and() , .or() 으로 조립해서 최종적인 WHERE절을 만듬
  * */
  private BooleanExpression cursorCondition(String cursor, Instant after, String orderBy, Sort.Direction direction) {
    if (cursor == null || cursor.isBlank()) {
      return null; // 첫 페이지 조회 시 커서가 없으므로 조건 생략함
    }
    // 정렬 기준에 따라 커서 데이터 타입이 다르기 때문에 분기 처리
    if ("rating".equals(orderBy)) {
      double ratingCursor = Double.parseDouble(cursor);
      if (direction == Sort.Direction.ASC) {
        return book.rating.gt(ratingCursor)
            .or(book.rating.eq(ratingCursor).and(after != null ? book.createdAt.gt(after) : null));
      } else {
        return book.rating.lt(ratingCursor)
            .or(book.rating.eq(ratingCursor).and(after != null ? book.createdAt.lt(after) : null));
        /* 해석 예시
        * 방금 내가 본 책의 평점(ratingCursor)보다 작은(lt) 책들을 찾아달라.
        * 또는(or), 만약 평점이 완전히 똑같고(eq), 그리고(and) 생성 시간이 방금 본 책(after)보다 더 옛날인(lt) 책들을 순서대로 찾아달라.
        * */
      }
    } else if ("reviewCount".equals(orderBy)) {
      int countCursor = Integer.parseInt(cursor);
      if (direction == Sort.Direction.ASC) {
        return book.reviewCount.gt(countCursor)
            .or(book.reviewCount.eq(countCursor).and(after != null ? book.createdAt.gt(after) : null));
      } else {
        return book.reviewCount.lt(countCursor)
            .or(book.reviewCount.eq(countCursor).and(after != null ? book.createdAt.lt(after) : null));
      }
    } else if ("publishedDate".equals(orderBy)) {
      LocalDate dateCursor = LocalDate.parse(cursor);
      if (direction == Sort.Direction.ASC) {
        return book.publishedDate.gt(dateCursor)
            .or(book.publishedDate.eq(dateCursor).and(after != null ? book.createdAt.gt(after) : null));
      } else {
        return book.publishedDate.lt(dateCursor)
            .or(book.publishedDate.eq(dateCursor).and(after != null ? book.createdAt.lt(after) : null));
      }
    } else if ("title".equals(orderBy)) {
      if (direction == Sort.Direction.ASC) {
        return book.title.gt(cursor)
            .or(book.title.eq(cursor).and(after != null ? book.createdAt.gt(after) : null));
      } else {
        return book.title.lt(cursor)
            .or(book.title.eq(cursor).and(after != null ? book.createdAt.lt(after) : null));
      }
    } else {
      // 기본 정렬 (createdAt)
      Instant timeCursor = Instant.parse(cursor);
      if (direction == Sort.Direction.ASC) {
        return book.createdAt.gt(timeCursor);
      } else {
        return book.createdAt.lt(timeCursor);
      }
    }
  }

  // 동적 정렬 객체 생성
  /*
  * OrderSpecifier : QueryDSL에서 ORDER BY (정렬)조건을 담는 객체. 어떤 컬럼을 어느 방향으로 정렬할 지 정보를 가지고 있음
  * OrderSpecifier<?>[] : 정렬 조건을 타입은 신경쓰지 않고, 1순위 정렬, 2순위 정렬 등 여러 정렬 조건을 배열로 묶어서 저장하겠다.
  * */
  private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, Sort.Direction direction) {
    List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
    Order targetOrder = direction.isAscending() ? Order.ASC : Order.DESC;

    // 1순위 정렬 조건
    if ("rating".equals(orderBy)) {
      orderSpecifiers.add(new OrderSpecifier<>(targetOrder, book.rating));
    } else if ("reviewCount".equals(orderBy)) {
      orderSpecifiers.add(new OrderSpecifier<>(targetOrder, book.reviewCount));
    } else if ("publishedDate".equals(orderBy)) {
      orderSpecifiers.add(new OrderSpecifier<>(targetOrder, book.publishedDate));
    } else if ("title".equals(orderBy)) {
      orderSpecifiers.add(new OrderSpecifier<>(targetOrder, book.title));
    }

    // 2순위 정렬 조건 (생성 시간으로 고유 보조 정렬)
    orderSpecifiers.add(new OrderSpecifier<>(targetOrder, book.createdAt));

    return orderSpecifiers.toArray(new OrderSpecifier[0]);
  }
}