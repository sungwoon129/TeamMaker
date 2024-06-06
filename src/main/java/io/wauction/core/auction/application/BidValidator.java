package io.wauction.core.auction.application;

import io.wauction.core.auction.dto.AuctionError;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.document.Bid;
import io.wauction.core.auction.entity.table.AuctionItem;
import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.entity.table.ParticipantRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class BidValidator {

    public List<AuctionError> validateBidRequest(BidRequest bidRequest, AuctionPlayItem auctionPlayItem, AuctionRule auctionRule, List<AuctionPlayItem> auctionPlayItems) {
        List<AuctionError> errors = new ArrayList<>();

        if(bidRequest.getItemId() != auctionPlayItem.getItemId()) errors.add(new AuctionError("현재 경매가 진행중인 대상에 대해서만 입찰이 가능합니다."));
        if(!bidRequest.getMessage().chars().allMatch(Character::isDigit)) errors.add(new AuctionError("입찰 가격은 숫자만 허용됩니다."));

        ParticipantRole bidder = auctionRule.getRoles().stream()
                .filter(role -> role.getName().equals(bidRequest.getSender()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(bidRequest.getSender() + "와 일치하는 참가자 정보를 찾을 수 없습니다."));

        if(bidder.getPoint().getValue() < bidRequest.getPrice()) errors.add(new AuctionError("보유한 포인트보다 더 높은 포인트를 입찰할 수 없습니다."));


        AuctionItem auctionItem = auctionRule.getAuctionItems().stream().filter(item -> item.getId() == bidRequest.getItemId()).findAny().orElseThrow(() -> new IllegalArgumentException("현재 진행되는 경매에 요청한 ID와 일치하는 경매 대상이 존재하지 않습니다."));
        List<AuctionPlayItem> purchasedItems  = auctionPlayItems.stream().filter(playItem -> playItem.getWinningBidder().equals(bidRequest.getSender())).toList();

        Optional<AuctionPlayItem> duplicatedPosition = purchasedItems.stream().filter(playItem -> playItem.getTeamPosition().getName().equals(auctionItem.getPosition().getName())).findAny();

        if(duplicatedPosition.isPresent()) errors.add(new AuctionError("이미 낙찰받은 대상과 중복되는 포지션의 대상에 입찰할 수 없습니다."));

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
