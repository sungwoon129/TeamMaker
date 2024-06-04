package io.wauction.core.auction.application;

import io.wauction.core.auction.dto.AuctionError;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.document.Bid;
import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.entity.table.ParticipantRole;

import java.util.ArrayList;
import java.util.List;


public class BidValidator {

    public List<AuctionError> validateBidRequest(BidRequest bidRequest, AuctionPlayItem auctionPlayItem, AuctionRule auctionRule) {
        List<AuctionError> errors = new ArrayList<>();

        if(bidRequest.getItemId() != auctionPlayItem.getItemId()) errors.add(new AuctionError("현재 경매가 진행중인 대상에 대해서만 입찰이 가능합니다."));
        if(!bidRequest.getMessage().chars().allMatch(Character::isDigit)) errors.add(new AuctionError("입찰 가격은 숫자만 허용됩니다."));

        ParticipantRole bidder = auctionRule.getRoles().stream()
                .filter(role -> role.getName().equals(bidRequest.getSender()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(bidRequest.getSender() + "와 일치하는 참가자 정보를 찾을 수 없습니다."));

        if(bidder.getPoint().getValue() < bidRequest.getPrice()) errors.add(new AuctionError("보유한 포인트보다 더 높은 포인트를 입찰할 수 없습니다."));

        return errors;
    }

    public List<AuctionError> validateBidCondition(Bid biggestBid, BidRequest bidRequest) {
        List<AuctionError> errors = new ArrayList<>();

        if(biggestBid.priceIsEqualsOrGraterThan(bidRequest.getPrice())) {
            errors.add(new AuctionError("새로운 입찰 금액은 이전 최고가보다 높아야 합니다."));
        }

        if(biggestBid.getBidder().equals(bidRequest.getSender())) {
            errors.add(new AuctionError("현재 최고 입찰자는 입찰할 수 없습니다."));
        }

        return errors;
    }


}
