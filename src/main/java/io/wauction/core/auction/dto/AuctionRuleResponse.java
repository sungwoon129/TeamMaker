package io.wauction.core.auction.dto;

import io.wauction.core.auction.entity.ProceedWay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class AuctionRuleResponse {
    private Long id;
    private List<ParticipantRoleResponse> roles;
    private List<TeamPositionResponse> positions;
    private List<AuctionItemResponse> items;
    private ProceedWay proceedWay;
    private int startOrder;


    public void shuffleItems() {
        List<AuctionItemResponse> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        this.items = copy;
    }

}
