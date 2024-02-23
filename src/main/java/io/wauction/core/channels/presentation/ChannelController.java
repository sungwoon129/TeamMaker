package io.wauction.core.channels.presentation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
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
    private final ObjectMapper objectMapper = new ObjectMapper();


    @MessageMapping("/channel/{channelId}/send")
    @SendTo("/channel/{channelId}")
    public void send(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String destination = "/channel/" + channelId;
        String msg = MessageType.findByTitle(messageRequest.getType()).makeFullMessage(messageRequest.getMessage());

        simpMessagingTemplate.convertAndSend(destination, new MessageResponse(messageRequest.getSender(), msg));
    }

    @MessageMapping("/channel/{channelId}/ready")
    @SendTo("/channel/{channelId}")
    public void ready(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) {

        // TODO : 채널에 속한 사람들의 준비상태 check

        String destination = "/channel/" + channelId;
        String msg = MessageType.findByTitle(messageRequest.getType()).makeFullMessage(messageRequest.getMessage());

        simpMessagingTemplate.convertAndSend(destination, new MessageResponse(messageRequest.getSender(), msg));
    }
}
