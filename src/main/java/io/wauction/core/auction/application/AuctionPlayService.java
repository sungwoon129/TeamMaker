package io.wauction.core.auction.application;

import io.wauction.core.auction.dto.AuctionError;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.document.AuctionOrder;
import io.wauction.core.auction.entity.document.Bid;
import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.entity.table.ParticipantRole;
import io.wauction.core.auction.infrastructure.AuctionOrderRepository;
import io.wauction.core.auction.infrastructure.AuctionRuleRepository;
import io.wauction.core.auction.infrastructure.BidRepository;
import io.wauction.core.common.exception.BidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static io.wauction.core.auction.entity.document.Bid.createBid;


@RequiredArgsConstructor
@Service
public class AuctionPlayService {

    private final AuctionRuleRepository auctionRuleRepository;
    private final BidRepository bidRepository;
    private final AuctionOrderRepository auctionOrderRepository;

    public void saveBid(BidRequest bidRequest, long channelId, long auctionRuleId) {


        Optional<Bid> biggestBid = bidRepository.findTopByChannelIdAndItemIdOrderByPriceDesc(channelId, bidRequest.getItemId());

        if(biggestBid.isPresent()) {

            BidValidator bidValidator = new BidValidator();
            List<AuctionError> errors = bidValidator.validateBidCondition(biggestBid.get(), bidRequest);

            if(!errors.isEmpty()) {
                throw new BidException(errors);
            }
        }

        bidRepository.save(createBid(channelId, auctionRuleId, bidRequest.getItemId(), bidRequest.getSender(), Long.parseLong(bidRequest.getMessage())));

    }

    //TODO 유찰 구현. 현재로서는 필요한 작업 X 확장성 위해 선언된 메소드
    public void failInBid() {
        // do nothing

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
