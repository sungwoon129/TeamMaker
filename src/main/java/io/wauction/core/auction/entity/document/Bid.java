package io.wauction.core.auction.entity.document;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collection = "bids")
public class Bid {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;
    private long channelId;
    private long auctionRuleId;
    private long itemId;
    private String bidder;
    private long price;

    protected Bid(long channelId, long auctionRuleId, long itemId, String bidder, long price) {
        this.channelId = channelId;
        this.auctionRuleId = auctionRuleId;
        this.itemId = itemId;
        this.bidder = bidder;
        this.price = price;
    }

    public static Bid createBid(long channelId, long auctionRuleId, long itemId, String bidder, long price) {
        return new Bid(channelId, auctionRuleId, itemId, bidder, price);
    }

    public boolean priceIsEqualsOrGraterThan(long price) {
        return this.price >= price;
    }
}
