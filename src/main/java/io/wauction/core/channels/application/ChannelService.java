package io.wauction.core.channels.application;

import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;

    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    public Channel findOne(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(""));
    }

    public void create(ChannelRequest channelRequest) {
        Channel channel = new Channel();
        channelRepository.save(channel);
    }
}
