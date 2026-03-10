package com.codeit.project.sb08deokhugamteamgwanwoong.service.external;

import static org.assertj.core.api.Assertions.assertThat;

import com.codeit.project.sb08deokhugamteamgwanwoong.dto.book.BookDto;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class MockBookProviderTest {

  // 객체 직접 생성해서 테스트
  private final MockBookProvider mockBookProvider = new MockBookProvider();

  @DisplayName("MockBookProvider는 항상 고정된 더미 BookDto를 반환한다.")
  @Test
  void getBookMetadata_Success() {
    // given
    String query = "스프링부트 뽀개기";

    // when
    BookDto result = mockBookProvider.getBookMetadata(query);

    // then
    assertThat(result.title()).isEqualTo("Mock Book: " + query);
    assertThat(result.author()).isEqualTo("Mock Author");
    assertThat(result.publisher()).isEqualTo("Mock Publisher");
    assertThat(result.isbn()).isEqualTo("1111111111");
    assertThat(result.description()).isEqualTo("This is a mock book description.");
    assertThat(result.thumbnailUrl()).isEqualTo("https://via.placeholder.com/150");
    assertThat(result.publishedDate()).isEqualTo(LocalDate.now());
  }

}
