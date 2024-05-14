package io.wauction.core.auction.dto;

import io.wauction.core.auction.entity.table.ProceedWay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

}
