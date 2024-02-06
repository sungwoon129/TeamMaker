package io.wauction.core.channels.presentation;

import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.channels.dto.MessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Slf4j
@Controller
public class AuctionController {

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public MessageResponse greeting(MessageRequest messageRequest) throws InterruptedException {
        Thread.sleep(1000);

        MessageResponse response = new MessageResponse("test",messageRequest.getMessage());

        return response;
    }
}
