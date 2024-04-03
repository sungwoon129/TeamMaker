package io.wauction.core.channels.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.auction.entity.ParticipantRole;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.EnterMessageResponse;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

import static io.wauction.core.channels.event.StompEventHandler.subscribeMap;


@Slf4j
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

    public void requestForRoleExchange(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getSender()), messageRequest.getTargetUsername());

        publishMessageToUser(channelId, messageRequest.getTargetUsername(), messageResponse);

    }

    public void responseRoleExchangeRequest(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE_RES, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), messageRequest.getTargetUsername(), messageRequest.getMessage());


/*        if (messageRequest.getMessage().equals("Y")) {
            List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.debug("before swap = " + c.getSessionId() + " : " + c.getRole()));

            for (ChannelConnection connection : connections) {
                if (connection.getRole().equals(messageRequest.getTargetUsername())) {
                    connection.setRole(messageRequest.getSender());
                } else if (connection.getRole().equals(messageRequest.getSender())) {
                    connection.setRole(messageRequest.getTargetUsername());
                }
            }

            subscribeMap.put(String.valueOf(channelId), connections);

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.debug("after swap = " + c.getSessionId() + " : " + c.getRole()));
        }*/

        publishMessageToUser(channelId, messageRequest.getTargetUsername(), messageResponse);
    }

    public void publishMessageToChannel(long channelId, Object messageResponseDto) {
        String destination = "/channel/" + channelId;

        try {
            String resultMsg = objectMapper.writeValueAsString(messageResponseDto);
            simpMessageSendingOperations.convertAndSend(destination, resultMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public void publishMessageToUser(long channelId, String targetUser, Object messageResponseDto) {
        String destination = "/channel" +
                "/" +
                channelId +
                "/" +
                targetUser +
                "/secured";

        try {
            String resultMsg = objectMapper.writeValueAsString(messageResponseDto);
            simpMessageSendingOperations.convertAndSend(destination, resultMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }


}
