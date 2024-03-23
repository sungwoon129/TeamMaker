package io.wauction.core.channels.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.auction.entity.ParticipantRole;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.EnterMessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;



@RequiredArgsConstructor
@Service
public class ChannelService {

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final ChannelRepository channelRepository;
    private final AuctionRuleService auctionRuleService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<Channel> findAll() {
        return channelRepository.findByDeletedIsNullOrDeletedFalse();
    }

    public Channel findOne(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 ID 입니다."));
    }

    public void save(Channel channel) {
        channelRepository.save(channel);
    }

    @Transactional
    public String enter(long channelId, String sender) {
        Channel channel = findOne(channelId);
        channel.enter();

        ParticipantRole role = channel.getAuctionRule().getRoles().stream().filter(r -> r.getName().equals(sender)).findFirst().orElseThrow(() -> new NoSuchElementException("메시지 전송자를 유효한 역할에서 찾을 수 없습니다."));

        return role.getName();

    }

    @Transactional
    public void leave(long channelId, String sender, List<ChannelConnection> connections) {
        Channel channel = findOne(channelId);
        channel.leave();

        List<String> activeRoles = connections.stream().map(ChannelConnection::getRole).toList();

        EnterMessageResponse responseDto = new EnterMessageResponse(MessageType.LEAVE, "SYSTEM", sender, MessageType.LEAVE.makeFullMessage(sender), activeRoles);
        publishMessageToChannel(channel.getId(), responseDto);
    }

    public Channel countReady(long channelId) {
        Channel channel = findOne(channelId);

        channel.addReadyCount();

        return channel;
    }

    public void publishMessageToChannel(long channelId, Object messageResponseDto)  {
        String destination = "/channel/" + channelId;

        try {
            String resultMsg = objectMapper.writeValueAsString(messageResponseDto);
            simpMessageSendingOperations.convertAndSend(destination, resultMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}
