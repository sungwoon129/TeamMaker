package io.wauction.core.auction.entity;

import io.wauction.core.auction.dto.AuctionPlayItem;

import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collation = "auctionOrder")
public class AuctionOrder {

    @Transient
    public static final String SEQUENCE_NAME = "order_sequence";

    @Id
    private long id;

    private long seq;

    private long channelId;

    private List<AuctionPlayItem> items;

    protected AuctionOrder(long id, long channelId, List<AuctionPlayItem> items) {
        this.channelId = channelId;
        this.items = items;
    }


    public static AuctionOrder createAuctionOrder(long id, long channelId, List<AuctionPlayItem> items) {
        return new AuctionOrder(id, channelId, items);
    }

}
