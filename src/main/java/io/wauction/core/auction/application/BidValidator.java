package io.wauction.core.auction.application;

import io.wauction.core.auction.dto.AuctionError;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;

import java.util.ArrayList;
import java.util.List;


public class BidValidator {

    public List<AuctionError> validate(BidRequest bidRequest, AuctionPlayItem auctionPlayItem) {
        List<AuctionError> errors = new ArrayList<>();

        if(bidRequest.getItemId() != auctionPlayItem.getItemId()) errors.add(new AuctionError("현재 경매가 진행중인 대상에 대해서만 입찰이 가능합니다."));
        if(!bidRequest.getMessage().chars().allMatch(Character::isDigit)) errors.add(new AuctionError("입찰 가격은 숫자만 허용됩니다."));

        return errors;
    }

}
