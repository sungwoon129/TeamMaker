package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChannelRequest {

    private String name;
    private String waitingTime;
    private String auctionTime;
    private long auctionRuleId;

}
