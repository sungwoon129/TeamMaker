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
    // TODO : 좀더 괜찮은 경매순서 관리방법 생각해볼 필요 있음. 순서를 서버에서 컨트롤하기 위해 어디서 관리해야하는지
    private int order; // 진행 중(해야하는) 경매 아이템 순서


    public void shuffleItems() {
        List<AuctionItemResponse> copy = new ArrayList<>(items);
        Collections.shuffle(copy);
        this.items = copy;
    }

}
