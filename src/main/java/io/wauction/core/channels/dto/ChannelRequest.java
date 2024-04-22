package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ChannelRequest {

    private String name;
    private int waitingTimeForNext = 5;
    private int waitingTimeForAfterBid = 5;
    private String auctionTime;
    private long auctionRuleId;

}
