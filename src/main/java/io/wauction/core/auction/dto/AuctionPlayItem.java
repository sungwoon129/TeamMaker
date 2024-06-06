package io.wauction.core.auction.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.wauction.core.auction.entity.table.ParticipantRole;
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
    private String winningBidder;
    private TeamPositionResponse teamPosition;


    public AuctionPlayItem(final long itemId, final String name, final TeamPositionResponse teamPosition) {
        this.itemId = itemId;
        this.name = name;
        this.teamPosition = teamPosition;
    }

    public boolean isNotSold(int currentOrder) {
        return winningBidder == null && currentOrder >= order;
    }

}
