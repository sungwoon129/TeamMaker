package io.wauction.core.channels.application;

import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static io.wauction.core.channels.entity.Channel.createChannel;

@RequiredArgsConstructor
@Service
public class ChannelService {

    private final ChannelRepository channelRepository;

    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    public Channel findOne(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 ID 입니다."));
    }

    public Long create(ChannelRequest channelRequest) {
        Channel channel = createChannel(channelRequest);
        channelRepository.save(channel);

        return channel.getId();
    }
}
