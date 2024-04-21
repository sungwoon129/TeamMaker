package io.wauction.core.auction.infrastructure;

import io.wauction.core.auction.entity.AuctionOrder;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuctionOrderRepository extends MongoRepository<AuctionOrder, Long> {
    Optional<AuctionOrder> findByChannelId(long channelId);
}
