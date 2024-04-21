package io.wauction.core.channels.application;

import io.wauction.core.auction.application.AuctionPlayService;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.AuctionOrder;
import io.wauction.core.auction.infrastructure.AuctionOrderRepository;
import io.wauction.core.channels.dto.DataMessageResponse;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChannelAuctionService {

    private final AuctionPlayService auctionPlayService;
    private final AuctionOrderRepository auctionOrderRepository;
    private final ChannelService channelService;

    public void bid(BidRequest bidRequest, long channelId) {

        Channel channel = channelService.findOne(channelId);

        auctionPlayService.saveBid(bidRequest, channel.getId(), channel.getAuctionRule().getId());

        MessageResponse messageResponse = new DataMessageResponse<>(
                MessageType.BID,
                bidRequest.getSender(),
                MessageType.BID.makeFullMessage(bidRequest.getMessage()),
                AuctionPlayItem.builder().itemId(bidRequest.getItemId()).name(bidRequest.getItemName()).price(Long.parseLong(bidRequest.getMessage())).build()
        );

        channelService.publishMessageToChannel(channelId,messageResponse);
    }

    @Transactional
    public void nextStep(long channelId) {
        Channel channel = channelService.findOne(channelId);

        channel.nextStep();

        AuctionOrder auctionOrder = auctionOrderRepository.findByChannelId(channelId).orElseThrow(() -> new IllegalArgumentException(channelId + " 와 일치하는 경매순서 데이터를 찾을 수 없습니다."));

        AuctionPlayItem auctionPlayItem = auctionOrder.getItems().get(channel.getOrder());

        MessageResponse messageResponse = new DataMessageResponse<>(
                MessageType.NEXT,
                "SYSTEM",
                MessageType.NEXT.makeFullMessage(auctionPlayItem.getName()),
                auctionPlayItem
        );

        channelService.publishMessageToChannel(channelId, messageResponse);
    }
}
