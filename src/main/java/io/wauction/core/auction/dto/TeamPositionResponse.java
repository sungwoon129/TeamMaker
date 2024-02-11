package io.wauction.core.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class TeamPositionResponse {
    private Long id;
    private String name;
}
