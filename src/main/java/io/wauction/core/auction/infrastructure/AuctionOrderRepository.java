package io.wauction.core.auction.infrastructure;

import io.wauction.core.auction.entity.document.AuctionOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuctionOrderRepository extends MongoRepository<AuctionOrder, String> {
    Optional<AuctionOrder> findByChannelId(long channelId);

}
