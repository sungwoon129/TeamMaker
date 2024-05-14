package io.wauction.core.auction.application;

import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.infrastructure.AuctionRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuctionRuleService {

    private final AuctionRuleRepository auctionRuleRepository;


    public AuctionRule getDefaultRule() {
        Optional<AuctionRule> defaultRule = auctionRuleRepository.findFirstByOrderByCreatedTimeAsc();

        return defaultRule.orElseThrow(() -> new IllegalArgumentException("등록된 기본 경매 룰이 존재하지 않습니다."));
    }

    public AuctionRule findById(long auctionRuleId) {

        return auctionRuleRepository.findById(auctionRuleId).orElseThrow(() -> new IllegalArgumentException("요청한 ID와 동일한 경매 룰이 존재하지 않습니다."));
    }
}
