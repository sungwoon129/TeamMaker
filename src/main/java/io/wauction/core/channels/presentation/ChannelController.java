package io.wauction.core.channels.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.dto.ReadyMessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

import static io.wauction.core.channels.event.StompEventHandler.subscribeMap;

@RequiredArgsConstructor
@Slf4j
@Controller
public class ChannelController {

    /**
     * @SendTo 대신 SimpMessagingTemplate를 사용한 이유 : Spring에서 기본적으로 제공하는 브로커를 추후 교체할 때 변경해야하는 부분을 최소화하기 위해
     */
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChannelService channelService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping("/channel/{channelId}/exchangeSeat")
    public void exchangeSeatRequest(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.EXCHANGE) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        String destination = "/channel" +
                "/" +
                channelId +
                "/" +
                messageRequest.getTargetUsername() +
                "/secured";

        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getSender()), messageRequest.getTargetUsername());
        String resultMsg = objectMapper.writeValueAsString(messageResponse);


        simpMessagingTemplate.convertAndSend(destination, resultMsg);
    }

    @MessageMapping("/channel/{channelId}/acceptChange")
    public void acceptExchange(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.EXCHANGE) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        String destination = "/private";
        MessageResponse messageResponse = new MessageResponse(MessageType.EXCHANGE, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), messageRequest.getTargetUsername(), messageRequest.getMessage());
        String resultMsg = objectMapper.writeValueAsString(messageResponse);

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));
        ChannelConnection channelConnection = connections.stream()
                .filter(connection -> connection.getRole().equals(messageRequest.getTargetUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("요청한 역할을 수행하는 사용자가 존재하지 않습니다."));

        // TODO : 만약 변경 동의했다면 connections 내부의 세션값 스왑.

        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(channelConnection.getSessionId());
        headerAccessor.setLeaveMutable(true);

        simpMessagingTemplate.convertAndSendToUser(channelConnection.getSessionId(),destination, resultMsg,headerAccessor.getMessageHeaders());
    }



    @MessageMapping("/channel/{channelId}/send")
    public void bid(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        String resultMsg = objectMapper.writeValueAsString(new MessageResponse(messageType, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage())));

        simpMessagingTemplate.convertAndSend(destination, resultMsg);
    }

    @MessageMapping("/channel/{channelId}/ready")
    public void ready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        Channel channel = channelService.countReady(channelId);

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        String resultMsg = objectMapper.writeValueAsString(new ReadyMessageResponse(messageType,messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), channel.getReadyCount(), channel.getCapacity()));

        simpMessagingTemplate.convertAndSend(destination, resultMsg);
    }
}
