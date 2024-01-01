package io.wauction.core.channels.presentation;

import io.wauction.core.channels.application.BidService;
import io.wauction.core.channels.dto.AuctionRequest;
import io.wauction.core.channels.dto.MessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Slf4j
@Controller
public class AuctionController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BidService bidService;

    @MessageMapping("/channels/{channelId}/messages")
    public void chat(@DestinationVariable Long channelId, AuctionRequest auctionRequest) {
        simpMessagingTemplate.convertAndSend("/subscription/channels/" + channelId, auctionRequest.getContent());
        log.info("Message [{}] send by member: {} to room: {}", auctionRequest.getContent(), auctionRequest.getSenderId(), channelId);
    }

    @MessageMapping("/channels/{channelId}/bid")
    public void bid(@DestinationVariable Long channelId, MessageRequest messageRequest) {
        bidService.bid(messageRequest);
        log.info("Message [{}] send by member: {} to room: {}", messageRequest.getMessage(), messageRequest.getSenderId(), channelId);
    }
}
