package io.wauction.core.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Builder
@AllArgsConstructor
public class AuctionItemResponse {
    private Long id;
    private String name;
    private List<HighlightResponse> highlights;

}
