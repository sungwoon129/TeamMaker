package io.wauction.core.channels.application;

import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.entity.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.wauction.core.channels.entity.Channel.createChannel;

@RequiredArgsConstructor
@Service
public class CreateAuctionRoomService {

    private final AuctionRuleService auctionRuleService;
    private final ChannelService channelService;


    public Long createAuctionRoom(ChannelRequest channelRequest) {
        Channel channel = createChannel(channelRequest, auctionRuleService);
        channelService.save(channel);

        return channel.getId();
    }


}
