package io.wauction.core.channels.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.channels.application.ChannelService;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

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
    public void requestForRoleExchange(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.EXCHANGE) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelService.requestForRoleExchange(channelId, messageRequest, messageType);
    }

    @MessageMapping("/channel/{channelId}/role-exchange/response")
    public void responseRoleExchangeRequest(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.EXCHANGE_RES) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelService.responseRoleExchangeRequest(channelId, messageRequest, messageType);

    }



    @MessageMapping("/channel/{channelId}/send")
    public void bid(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        String resultMsg = objectMapper.writeValueAsString(new MessageResponse(messageType, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage())));

        simpMessagingTemplate.convertAndSend(destination, resultMsg);
    }

    @MessageMapping("/channel/{channelId}/ready")
    public void ready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.READY) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelService.countReady(channelId, messageRequest);

    }
}
