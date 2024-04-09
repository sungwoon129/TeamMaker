package io.wauction.core.channels.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.auction.entity.ParticipantRole;
import io.wauction.core.channels.dto.*;
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
import java.util.Optional;

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

        EnterMessageResponse responseDto = EnterMessageResponse.builder()
                .messageType(MessageType.LEAVE)
                .writer("SYSTEM")
                .msg(MessageType.LEAVE.makeFullMessage(sender))
                .activeRoles(activeRoles)
                .build();

        publishMessageToChannel(channel.getId(), responseDto);
    }

    @Transactional
    public void countReady(long channelId, String sessionId, MessageRequest messageRequest, boolean isPlus) {

        Channel channel = this.findOne(channelId);

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        for(ChannelConnection channelConnection : connections) {
            if(channelConnection.getSessionId().equals(sessionId)) channelConnection.setReady(isPlus);
        }

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        this.publishMessageToChannel(channelId, new ReadyMessageResponse(
                messageType,
                messageRequest.getSender(),
                messageType.makeFullMessage(messageRequest.getMessage()),
                (int) connections.stream().filter(ChannelConnection::isReady).count(),
                channel.getCapacity()));
    }


    public void requestForRoleExchange(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getSender()), messageRequest.getTargetUsername());

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        Optional<ChannelConnection> connection = connections.stream().filter(connect -> connect.getRole().equals(messageRequest.getTargetUsername())).findAny();
        if(connection.isEmpty()) throw new IllegalArgumentException(messageRequest.getTargetUsername() + " 은(는) 올바른 메시지 수신자가 아닙니다.");

        publishMessageToUser(channelId, connection.get().getUid(), messageResponse);

    }

    public void responseRoleExchangeRequest(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE_RES, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), messageRequest.getTargetUsername(), messageRequest.getMessage());
        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        Optional<ChannelConnection> targetConnection = connections.stream().filter(connect -> connect.getRole().equals(messageRequest.getTargetUsername())).findAny();
        if(targetConnection.isEmpty()) throw new IllegalArgumentException(messageRequest.getTargetUsername() + " 은(는) 올바른 메시지 수신자가 아닙니다.");

        if (messageRequest.getMessage().equals("Y")) {

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.info("before swap = {} : {}", c.getRole(), c.getSessionId()));

            for (ChannelConnection connection : connections) {
                if (connection.getRole().equals(messageRequest.getTargetUsername())) {
                    connection.setRole(messageRequest.getSender());
                } else if (connection.getRole().equals(messageRequest.getSender())) {
                    connection.setRole(messageRequest.getTargetUsername());
                }
            }

            subscribeMap.put(String.valueOf(channelId), connections);

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.info("after swap = {} : {}", c.getRole(), c.getSessionId()));
        }


        publishMessageToUser(channelId, targetConnection.get().getUid(), messageResponse);
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
