package com.codeit.project.sb08deokhugamteamgwanwoong.dto.external.naver;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverApiResponse {
    private List<NaverItem> items;
}
