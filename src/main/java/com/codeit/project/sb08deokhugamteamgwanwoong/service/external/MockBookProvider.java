package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Primary // 개발 초기에는 이 녀석을 우선적으로 사용하게 설정 가능
@Profile("dev") // dev 프로파일에서만 활성화
public class MockBookProvider implements BookMetadataProvider {

    @Override
    public BookDto getBookMetadata(String query) {
        log.info("Fetching book metadata from Mock Provider for query: {}", query);

        return BookDto.builder()
            .title("Mock Book: " + query)
            .author("Mock Author")
            .publisher("Mock Publisher")
            .isbn("1111111111")
            .description("This is a mock book description.")
            .imageUrl("https://via.placeholder.com/150")
            .build();
    }
}
