package io.wauction.core.auction.infrastructure;

import io.wauction.core.auction.entity.Bid;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BidRepository extends MongoRepository<Bid, Long> {

    Optional<Bid> findTopByChannelIdAndItemIdOOrderByPriceDesc(Long channelId, Long itemId);
}
