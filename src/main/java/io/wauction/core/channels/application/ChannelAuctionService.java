package io.wauction.core.channels.application;

import io.wauction.core.auction.application.AuctionPlayService;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChannelAuctionService {

    private final AuctionPlayService auctionPlayService;
    private final ChannelService channelService;

    public void bid(BidRequest bidRequest, long channelId) {

        Channel channel = channelService.findOne(channelId);

        auctionPlayService.saveBid(bidRequest, channel.getId(), channel.getAuctionRule().getId());

        MessageResponse messageResponse = new MessageResponse(
                MessageType.BID,
                bidRequest.getSender(),
                MessageType.BID.makeFullMessage(bidRequest.getMessage())
        );

        channelService.publishMessageToChannel(channelId,messageResponse);
    }
}
