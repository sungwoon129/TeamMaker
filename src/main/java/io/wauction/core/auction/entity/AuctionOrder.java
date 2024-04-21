package io.wauction.core.auction.entity;

import io.wauction.core.auction.dto.AuctionPlayItem;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collation = "auctionOrder")
public class AuctionOrder {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    private long channelId;

    private List<AuctionPlayItem> items;

    protected AuctionOrder(long channelId, List<AuctionPlayItem> items) {
        this.channelId = channelId;
        this.items = items;
    }


    public static AuctionOrder createAuctionOrder(long channelId, List<AuctionPlayItem> items) {
        return new AuctionOrder(channelId, items);
    }

}
