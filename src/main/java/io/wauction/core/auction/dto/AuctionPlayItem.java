package io.wauction.core.auction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionPlayItem {
    private long itemId;
    private String name;
    private long price;
    private int order;
    private String successfulBidder;


    public AuctionPlayItem(final long itemId, final String name) {
        this.itemId = itemId;
        this.name = name;
    }

    public boolean isNotSold(int currentOrder) {
        return successfulBidder == null && currentOrder >= order;
    }

}
