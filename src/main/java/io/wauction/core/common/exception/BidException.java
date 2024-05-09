package io.wauction.core.common.exception;

import io.wauction.core.auction.dto.AuctionError;
import lombok.Getter;

import java.util.List;

@Getter
public class BidException extends RuntimeException {

    private final List<AuctionError> errors;
    public BidException(List<AuctionError> errors) {
        this.errors = errors;
    }
}
