package io.wauction.core.channels.dto;

import io.wauction.core.auction.dto.AuctionItemResponse;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.ParticipantRoleResponse;
import io.wauction.core.auction.dto.TeamPositionResponse;
import io.wauction.core.auction.entity.table.ProceedWay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class AuctionStartData {

    private List<ParticipantRoleResponse> roles;
    private List<TeamPositionResponse> positions;
    private List<AuctionItemResponse> items;
    private ProceedWay proceedWay;
    private int order;
    private AuctionPlayItem auctionPlayItem;
    private int waitingTimeForNext;
    private int waitingTimeForAfterBid;

    public void shuffleItems() {
        List<AuctionItemResponse> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        this.items = copy;
    }
}
