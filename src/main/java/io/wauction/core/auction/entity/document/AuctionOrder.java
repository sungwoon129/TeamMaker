package io.wauction.core.auction.entity.document;

import io.wauction.core.auction.dto.AuctionPlayItem;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Document(collection = "auctionOrder")
public class AuctionOrder {


    @Id
    private String id;


    private long channelId;

    private List<AuctionPlayItem> items;

    @CreatedDate
    private Date createdTime;

    @LastModifiedDate
    private Date lastModifiedTime;



    protected AuctionOrder(long channelId, List<AuctionPlayItem> items) {
        this.channelId = channelId;
        this.items = items;
    }


    public static AuctionOrder createAuctionOrder(long channelId, List<AuctionPlayItem> items) {
        return new AuctionOrder(channelId, items);
    }



}
