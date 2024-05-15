package io.wauction.core.auction.application;

import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.document.AuctionOrder;
import io.wauction.core.auction.entity.document.Bid;
import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.entity.table.ParticipantRole;
import io.wauction.core.auction.infrastructure.AuctionOrderRepository;
import io.wauction.core.auction.infrastructure.AuctionRuleRepository;
import io.wauction.core.auction.infrastructure.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.wauction.core.auction.entity.document.Bid.createBid;


@RequiredArgsConstructor
@Service
public class AuctionPlayService {

    private final AuctionRuleRepository auctionRuleRepository;
    private final BidRepository bidRepository;
    private final AuctionOrderRepository auctionOrderRepository;

    public void saveBid(BidRequest bidRequest, long channelId, long auctionRuleId) {

        AuctionRule auctionRule = findById(auctionRuleId);

        if(auctionRule.getAuctionItems().stream().noneMatch(item -> item.getId() == bidRequest.getItemId())) throw new IllegalArgumentException(bidRequest.getItemId() + "와 일치하는 아이템 정보를 찾을 수 없습니다.");

        ParticipantRole participant = auctionRule.getRoles().stream()
                .filter(role -> role.getName().equals(bidRequest.getSender()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(bidRequest.getSender() + "와 일치하는 참가자 정보를 찾을 수 없습니다."));

        if(participant.getPoint().getValue() < Long.parseLong(bidRequest.getMessage())) {
            throw new IllegalStateException("보유한 포인트보다 더 높은 포인트를 입찰할 수 없습니다.");
        }

        Optional<Bid> biggestBid = bidRepository.findTopByChannelIdAndItemIdOrderByPriceDesc(channelId, bidRequest.getItemId());

        if(biggestBid.isPresent() && biggestBid.get().priceIsEqualsOrGraterThan(Long.parseLong(bidRequest.getMessage()))) {
            throw new IllegalStateException("새로운 입찰 금액은 이전 최고가보다 높아야 합니다.");
        }

        bidRepository.save(createBid(channelId, auctionRuleId, bidRequest.getItemId(), bidRequest.getSender(), Long.parseLong(bidRequest.getMessage())));

    }

    //TODO 유찰 구현. 현재로서는 필요한 작업 X 확장성 위해 선언된 메소드
    public void failInBid() {

    }

    public AuctionPlayItem sold(Bid bid, AuctionOrder auctionOrder, AuctionRule auctionRule) {

        AuctionPlayItem target = null;

        for(AuctionPlayItem item : auctionOrder.getItems()) {
            if(item.getItemId() == bid.getItemId()) {
                item.setWinningBidder(bid.getBidder());
                target = item;
                target.setPrice(bid.getPrice());
            }
        }

        auctionOrderRepository.save(auctionOrder);
        if(target == null) throw new IllegalArgumentException("등록된 경매순서내에 입찰건과 일치하는 경매대상을 찾을 수 없습니다.");

        ParticipantRole participant = auctionRule.getRoles().stream()
                .filter(role -> role.getName().equals(bid.getBidder()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(bid.getBidder() + "와 일치하는 참가자 정보를 찾을 수 없습니다."));

        participant.deductPoint(bid.getPrice());

        return target;

    }

    private AuctionRule findById(long auctionRuleId) {
        return auctionRuleRepository.findById(auctionRuleId).orElseThrow(() -> new IllegalArgumentException(auctionRuleId + "와 일치하는 경매진행정보를 찾을 수 없습니다."));
    }


    public Optional<Bid> getHighestBid(long channelId, AuctionPlayItem auctionPlayItem) {
        return bidRepository.findTopByChannelIdAndItemIdOrderByPriceDesc(channelId, auctionPlayItem.getItemId());
    }
}
