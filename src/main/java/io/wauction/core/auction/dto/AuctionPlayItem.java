package io.wauction.core.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuctionPlayItem {
    private long itemId;
    private String name;
    private long price;

    public AuctionPlayItem(final long itemId, final String name) {
        this.itemId = itemId;
        this.name = name;
    }

}
