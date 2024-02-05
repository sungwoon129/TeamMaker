package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.BidService;
import io.wauction.core.channels.dto.AuctionRequest;
import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Slf4j
@Controller
public class AuctionController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BidService bidService;

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MessageResponse greeting(MessageRequest messageRequest) {

        return new MessageResponse("test",messageRequest.getMessage());
    }
}
