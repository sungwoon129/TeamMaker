package io.wauction.core.auction.dto;

import io.wauction.core.channels.dto.MessageRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BidRequest extends MessageRequest {
    private long itemId;
    private String itemName;


    public long getPrice() {
        return Long.parseLong(this.getMessage());
    }

}
