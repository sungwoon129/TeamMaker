package io.wauction.core.channels.dto;

import io.wauction.core.auction.dto.AuctionRuleResponse;
import io.wauction.core.auction.dto.ParticipantRoleResponse;
import io.wauction.core.channels.entity.ChannelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChannelResponse {

    private long channelId;
    private String name;
    private int capacity;
    private int headCount;
    private ChannelState channelState;
    private AuctionRuleResponse auctionRuleResponse;
    private int order;
    private ParticipantRoleResponse clientRole;
    private List<String> activeRoles;
    private List<String> readyRoles;


}
