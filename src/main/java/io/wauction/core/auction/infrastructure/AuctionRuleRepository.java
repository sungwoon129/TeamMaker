package io.wauction.core.auction.infrastructure;

import io.wauction.core.auction.entity.table.AuctionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuctionRuleRepository extends JpaRepository<AuctionRule, Long> {

    Optional<AuctionRule> findFirstByOrderByCreatedTimeAsc();
}
