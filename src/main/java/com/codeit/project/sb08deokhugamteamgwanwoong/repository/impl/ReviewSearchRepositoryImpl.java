package com.codeit.project.sb08deokhugamteamgwanwoong.repository.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewDocument;
import com.codeit.project.sb08deokhugamteamgwanwoong.dto.review.ReviewSearchCondition;
import com.codeit.project.sb08deokhugamteamgwanwoong.repository.ReviewSearchRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReviewSearchRepositoryImpl implements ReviewSearchRepositoryCustom {

    // JPA의 EntityManager 같은 역할을 수행
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchHits<ReviewDocument> searchByCursor(ReviewSearchCondition condition, Pageable pageable) {

        // 조건들을 조합하는 상자
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();

        // 조건 1: keyword 검색(리뷰 내용, 책 이름, 유저 닉네임 부분 일치)
        if (StringUtils.hasText(condition.keyword())) {
            String kw = condition.keyword().trim();
            // 키워드의 길이가 1일 경우 nori가 아닌 ngram
            if (kw.length() == 1) {
                boolQuery.must(m -> m.multiMatch(mm -> mm
                        .query(condition.keyword())
                        .fields("content.ngram", "book.title.ngram", "user.nickname.ngram")
                ));
            } else {
                // 키워드의 길이가 2이상일 경우 nori + ngram
                boolQuery.must(m -> m.multiMatch(mm -> mm
                        .query(condition.keyword())
                        .fields(
                                "content", "content.ngram",
                                "book.title", "book.title.ngram",
                                "user.nickname", "user.nickname.ngram"
                        )
                        .type(TextQueryType.Phrase)
                        // 단어 사이의 거리 허용치(slop)을 1~2 정도로 설정
                        .slop(2)
                ));
            }
        }

        // bookId 필터(완전 일치)
        if (condition.bookId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("book.id").value(condition.bookId().toString())));
        }

        // userId 필터(완전 일치)
        if (condition.userId() != null) {
            boolQuery.filter(f -> f.term(t -> t.field("user.id").value(condition.userId().toString())));
        }

        Query query = new Query.Builder().bool(boolQuery.build()).build();

        Sort sort;
        if ("rating".equals(condition.orderBy())) {
            sort = Sort.by(condition.direction(), "rating")
                    .and(Sort.by(Sort.Direction.DESC, "createdAt"));
        } else {
            sort = Sort.by(condition.direction(), "createdAt");
        }

        List<Object> searchAfter = new ArrayList<>();
        if (condition.cursor() != null && condition.after() != null) {
            if ("rating".equals(condition.orderBy())) {
                // 별점 정렬의 커서: [별점 값, 생성 일자]
                searchAfter.add(Integer.valueOf(condition.cursor()));
                searchAfter.add(condition.after().toEpochMilli()); // Instant를 ES가 읽을 수 있는 Long으로 변환
            } else {
                // 최신순 정렬의 커서 : 생성 일자
                searchAfter.add(condition.after().toEpochMilli());
            }
        }

        Highlight highlight = new Highlight(
                HighlightParameters.builder()
                        .withPreTags("<mark style=\"background-color: #ffe58f; font-weight: bold;\">") // 시작 태그
                        .withPostTags("</mark>") // 종료 태그
                        .build(),
                Arrays.asList(
                        new HighlightField("content"),
                        new HighlightField("content.ngram"),
                        new HighlightField("book.title"),
                        new HighlightField("book.title.ngram"),
                        new HighlightField("user.nickname"),
                        new HighlightField("user.nickname.ngram")
                )
        );
        HighlightQuery highlightQuery = new HighlightQuery(highlight, null);

        // 4. NativeQuery 조립 (JPA의 QueryDsl 역할)
        NativeQueryBuilder nativeQueryBuilder = new NativeQueryBuilder()
                .withQuery(query)
                .withSort(sort)
                .withPageable(pageable)
                .withHighlightQuery(highlightQuery);

        // 커서 값이 존재하면 search_after 넣음
        if (!searchAfter.isEmpty()) {
            nativeQueryBuilder.withSearchAfter(searchAfter);
        }

        // 5. 쿼리 실행 및 반환
        return elasticsearchOperations.search(nativeQueryBuilder.build(), ReviewDocument.class);
    }
}
