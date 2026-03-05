package com.codeit.project.sb08deokhugamteamgwanwoong.dto.external.naver;

import java.util.List;

public record NaverApiResponse(
    List<NaverBookItemResponse> items
) {
}
