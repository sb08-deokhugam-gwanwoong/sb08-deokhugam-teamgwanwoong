package com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl;

import static com.codeit.project.sb08deokhugamteamgwanwoong.entity.QComment.comment;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.comment.CommentSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.entity.Comment;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.CommentRepositoryCustom;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Comment> findAllByCursor(CommentSearchCondition condition) {
    return queryFactory
        .selectFrom(comment)
        .where(
            //해당 리뷰의 댓글만 필터링
            comment.review.id.eq(condition.reviewId()),
            //삭제되지 않은 댓글만
            comment.deletedAt.isNull(),
            //커서 기반 페이징 조건
            cursorCondition(condition.cursor(), condition.after())
        )
        .orderBy(comment.createdAt.desc(), comment.id.desc()) //최신순
        .limit(condition.limit() + 1) //다음 페이지 여부 확인을 위해 +1개 조회
        .fetch();
  }

  //커서 필터링 로직
  private BooleanExpression cursorCondition(String cursor, Instant after) {
    if (cursor == null || cursor.isBlank()) {
      return null; // 첫 페이지 조회 시 조건 없음
    }

    Instant timeCursor = Instant.parse(cursor);

    return comment.createdAt.lt(timeCursor)
        .or(comment.createdAt.eq(timeCursor)
            .and(after != null ? comment.createdAt.lt(after) : null));
  }
}
