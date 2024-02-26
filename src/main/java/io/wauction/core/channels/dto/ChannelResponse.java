package io.wauction.core.channels.dto;

import io.wauction.core.auction.dto.AuctionRuleResponse;
import io.wauction.core.channels.entity.ChannelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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

}
