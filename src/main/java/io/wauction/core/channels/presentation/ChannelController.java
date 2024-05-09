package io.wauction.core.channels.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.channels.application.ChannelAuctionService;
import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.entity.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
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
    private final ChannelAuctionService channelAuctionService;
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


    @MessageMapping("/channel/{channelId}/ready")
    public void ready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest, StompHeaderAccessor headerAccessor) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.READY) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelService.countReady(channelId, headerAccessor.getSessionId(), messageRequest, true);

    }

    @MessageMapping("/channel/{channelId}/unready")
    public void unready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest, StompHeaderAccessor headerAccessor) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        if(messageType != MessageType.UNREADY) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelService.countReady(channelId, headerAccessor.getSessionId(), messageRequest, false);

    }

    @MessageMapping("/channel/{channelId}/start")
    public void start(@DestinationVariable long channelId, StompHeaderAccessor headerAccessor) {

        channelService.start(channelId, headerAccessor.getSessionId());

    }

    @MessageMapping("/channel/{channelId}/next")
    public void next(@DestinationVariable long channelId) {
        channelAuctionService.nextStep(channelId);
    }

    @MessageMapping("/channel/{channelId}/bid")
    public void next(@DestinationVariable long channelId, BidRequest bidRequest) {

        MessageType messageType = MessageType.findByTitle(bidRequest.getType());

        if(messageType != MessageType.BID) throw new IllegalArgumentException("올바른 메시지 타입이 아닙니다.");

        channelAuctionService.bid(bidRequest, channelId);
    }


    @MessageMapping("/channel/{channelId}/item/timer-end")
    public void timerEnd(@DestinationVariable long channelId, @Payload MessageRequest messageRequest, StompHeaderAccessor headerAccessor) {

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        channelAuctionService.timerEnd(channelId, headerAccessor.getSessionId(), messageType);

    }



    @MessageMapping("/channel/{channelId}/item/complete-highlight-play")
    public void completeHighlightPlay(@DestinationVariable long channelId, StompHeaderAccessor headerAccessor) {

        channelAuctionService.completeHighlightPlay(channelId, headerAccessor.getSessionId());

    }

    @MessageMapping("/channel/{channelId}/item/determine-destination")
    public void determineDestination(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {


    }
}
