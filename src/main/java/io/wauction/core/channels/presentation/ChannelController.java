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

    @MessageMapping("/channel/{channelId}/greeting")
    public void greeting(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String greetingMessage = MessageType.findByTitle(messageRequest.getType()).makeFullMessage(messageRequest.getSender());
        String destination = "/channel/" + channelId;
        String msg = objectMapper.writeValueAsString(new MessageResponse("SYSTEM", greetingMessage));

        simpMessagingTemplate.convertAndSend(destination,msg );
    }

    @MessageMapping("/channel/{channelId}/bid")
    @SendTo("/channel/{channelId}")
    public void bid(@DestinationVariable long channelId, @Payload MessageRequest messageRequest) throws JsonProcessingException {

        String destination = "/channel/" + channelId;
        String bidMessage = MessageType.findByTitle(messageRequest.getType()).makeFullMessage(messageRequest.getMessage());
        String msg = objectMapper.writeValueAsString(new MessageResponse(messageRequest.getSender(), bidMessage));

        simpMessagingTemplate.convertAndSend(destination, msg);
    }
}