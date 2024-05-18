package io.wauction.core.auction.dto;

import io.wauction.core.auction.entity.table.TeamPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuctionItemResponse {
    private Long id;
    private String name;
    private TeamPositionResponse position;
    private String img;
    private List<HighlightResponse> highlights;

}
