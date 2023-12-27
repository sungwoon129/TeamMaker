package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuctionRequest {

    private long senderId;
    private String content;
}
