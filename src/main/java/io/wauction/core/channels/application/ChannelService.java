package io.wauction.core.channels.application;

import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final AuctionRuleService auctionRuleService;

    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    public Channel findOne(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 ID 입니다."));
    }

    public void save(Channel channel) {
        channelRepository.save(channel);
    }

    public int enter(long channelId) {
        Channel channel = findOne(channelId);
        channel.enter();

        return channel.getHeadCount();
    }

    public Channel countReady(long channelId) {
        Channel channel = findOne(channelId);

        channel.addReadyCount();

        return channel;
    }
}
