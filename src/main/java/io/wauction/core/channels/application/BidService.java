package io.wauction.core.channels.application;

import io.wauction.core.channels.dto.MessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BidService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public void bid(MessageRequest messageRequest) {
        simpMessagingTemplate.convertAndSend("/subscription/channels/" + messageRequest.getChannelId(), messageRequest.getMessage());
    }
}
