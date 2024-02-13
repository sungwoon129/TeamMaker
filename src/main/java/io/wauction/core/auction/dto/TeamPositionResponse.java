package io.wauction.core.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TeamPositionResponse {
    private Long id;
    private String name;
    private String img;
}
