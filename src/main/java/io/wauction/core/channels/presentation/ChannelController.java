package io.wauction.core.channels.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.EnterMessageResponse;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.dto.ReadyMessageResponse;
import io.wauction.core.channels.entity.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Slf4j
@Controller
public class ChannelController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChannelService channelService;
    //private final ObjectMapper objectMapper = new ObjectMapper();


    @MessageMapping("/channel/{channelId}/enter")
    @SendTo("/channel/{channelId}")
    public void enter(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        int headCount = channelService.enter(channelId);

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        simpMessagingTemplate.convertAndSend(destination, new EnterMessageResponse(messageType, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), headCount));
    }

    @MessageMapping("/channel/{channelId}/send")
    @SendTo("/channel/{channelId}")
    public void bid(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        simpMessagingTemplate.convertAndSend(destination, new MessageResponse(messageType, messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage())));
    }

    @MessageMapping("/channel/{channelId}/ready")
    @SendTo("/channel/{channelId}")
    public void ready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        int readyCount = channelService.countReady(channelId);

        String destination = "/channel/" + channelId;
        MessageType messageType = MessageType.findByTitle(messageRequest.getType());



        simpMessagingTemplate.convertAndSend(destination, new ReadyMessageResponse(messageType,messageRequest.getSender(), messageType.makeFullMessage(messageRequest.getMessage()), readyCount));
    }
}
