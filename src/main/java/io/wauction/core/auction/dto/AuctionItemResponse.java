package io.wauction.core.auction.dto;

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
    private List<HighlightResponse> highlights;

}
