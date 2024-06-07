package io.wauction.core.channels.application;

import io.wauction.core.auction.application.AuctionPlayService;
import io.wauction.core.auction.application.BidValidator;
import io.wauction.core.auction.dto.AuctionError;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.BidRequest;
import io.wauction.core.auction.entity.document.AuctionOrder;
import io.wauction.core.auction.entity.document.Bid;
import io.wauction.core.auction.entity.table.AuctionRule;
import io.wauction.core.auction.infrastructure.AuctionOrderRepository;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.message.DataMessageResponse;
import io.wauction.core.channels.dto.message.MessageResponse;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.MessageType;
import io.wauction.core.common.exception.BidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.wauction.core.channels.event.StompEventHandler.subscribeMap;

/**
 * '다음 경매 대상 진행', '타이머 종료'등 경매 진행 중 경매 흐름에 대해 담당하면서 RDBMS의 데이터가 필요한 기능을 담당하는 클래스
 */
@RequiredArgsConstructor
@Service
public class ChannelAuctionService {

    private final AuctionPlayService auctionPlayService;
    private final AuctionOrderRepository auctionOrderRepository;
    private final ChannelService channelService;
    private final ScheduledExecutorService scheduledExecutorService;

    @Transactional
    public void bid(BidRequest bidRequest, long channelId) {

        Channel channel = channelService.findOne(channelId);

        AuctionOrder auctionOrder = auctionOrderRepository.findByChannelId(channelId).orElseThrow(() -> new IllegalArgumentException(channelId + " 와 일치하는 경매순서 데이터를 찾을 수 없습니다."));
        AuctionPlayItem auctionPlayItem = auctionOrder.getItems().get(channel.getOrderNum());
        AuctionRule auctionRule = channel.getAuctionRule();

        // 입찰요청 유효성 검사 1단계
        BidValidator bidValidator = new BidValidator();
        List<AuctionError> errors = bidValidator.validateBidRequest(bidRequest, auctionPlayItem, auctionRule, auctionOrder.getItems());

        if(!errors.isEmpty()) throw new BidException(errors);

        auctionPlayService.saveBid(bidRequest, channel.getId(), auctionRule.getId());

        MessageResponse messageResponse = new DataMessageResponse<>(
                MessageType.BID,
                bidRequest.getSender(),
                MessageType.BID.makeFullMessage(bidRequest.getMessage()),
                AuctionPlayItem.builder().itemId(bidRequest.getItemId()).name(bidRequest.getItemName()).price(Long.parseLong(bidRequest.getMessage())).build()
        );

        channelService.publishMessageToChannel(channelId,messageResponse);
    }

    public void nextItem(long channelId) {


        Channel channel = channelService.findOne(channelId);
        channel.nextStep();

        AuctionOrder auctionOrder = auctionOrderRepository.findByChannelId(channelId).orElseThrow(() -> new IllegalArgumentException(channelId + " 와 일치하는 경매순서 데이터를 찾을 수 없습니다."));

        initCompletionFlagOnChannel(channelId);

        AuctionPlayItem auctionPlayItem = auctionOrder.getItems().get(channel.getOrderNum());
        auctionPlayItem.setOrder(channel.getOrderNum());

        MessageResponse messageResponse = new DataMessageResponse<>(
                MessageType.NEXT,
                "SYSTEM",
                MessageType.NEXT.makeFullMessage(auctionPlayItem.getName()),
                auctionPlayItem
        );

        channelService.publishMessageToChannel(channelId, messageResponse);
    }

    public void completeHighlightPlay(long channelId, String sessionId) {
        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        connections.stream().filter(connect -> connect.getSessionId().equals(sessionId)).findAny().orElseThrow(() -> new IllegalStateException("현재 채널에 참가하지 않은 클라이언트의 요청입니다."));

        for(ChannelConnection connection : connections) {
            if(connection.getSessionId().equals(sessionId)) {
                connection.setCurrentHighlightCompleted(true);
            }
        }

        if(connections.stream().allMatch(ChannelConnection::isCurrentHighlightCompleted)) {

            MessageResponse messageResponse = MessageResponse.builder()
                    .messageType(MessageType.COMPLETE_HIGHLIGHT_PLAY)
                    .writer("SYSTEM")
                    .build();

            channelService.publishMessageToChannel(channelId, messageResponse);

        }
    }



    @Transactional
    public void timerEnd(long channelId, String sessionId, MessageType type) {

        /*List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));
        connections.stream().filter(connect -> connect.getSessionId().equals(sessionId)).findAny().orElseThrow(() -> new IllegalStateException("현재 채널에 참가하지 않은 클라이언트의 요청입니다."));*/

        if(isEveryoneOnChannelComplete(channelId)) {

            MessageResponse messageResponse = new DataMessageResponse<>(
                    MessageType.COMPLETE_COUNT,
                    "SYSTEM",
                    type.makeFullMessage(""),
                    type);


            channelService.publishMessageToChannel(channelId, messageResponse);


            // 입찰시간 타이머 종료
            if(type == MessageType.END_BID_TIMER) {

                Channel channel = channelService.findOne(channelId);

                if(!channel.isPlaying()) throw new IllegalStateException("타이머 종료요청은 경매 진행중에만 가능합니다.");

                determineDestination(channel);
            }
            // 입찰 전 대기시간 타이머 종료. 클라이언트의 연결불량, 메시지 전송 지연등 시간이 지나도 모든 채널의 참가자의 완료메시지가 오지 않아도 일정 시간이 지나면 다음 단계 진행
            else {
                executeChannelTimerEnd(channelId);
            }

        }
    }


    /**
     * 한 명의 경매대상의 입찰이 끝나고, 입찰결과에 따라 매물의 행선지(낙찰자팀 or 유찰목록) 결정
     * @param channel
     */
    private void determineDestination(Channel channel) {

        AuctionOrder auctionOrder = auctionOrderRepository.findByChannelId(channel.getId()).orElseThrow(() -> new IllegalArgumentException(channel.getId() + " 와 일치하는 경매순서 데이터를 찾을 수 없습니다."));
        AuctionPlayItem auctionPlayItem = auctionOrder.getItems().get(channel.getOrderNum());

        Optional<Bid> highestBid = auctionPlayService.getHighestBid(channel.getId(), auctionPlayItem);
        MessageType messageType;
        String msg;


        // 유찰
        if(highestBid.isEmpty()) {
            auctionPlayService.failInBid();
            messageType = MessageType.FAIL_IN_BID;
            msg = messageType.makeFullMessage(auctionPlayItem.getName());

        }
        // 낙찰
        else {
            AuctionPlayItem updated = auctionPlayService.sold(highestBid.get(), auctionOrder, channel.getAuctionRule());
            messageType = MessageType.SOLD;
            msg = messageType.makeFullMessage(updated.getName(), String.valueOf(updated.getPrice()));
            auctionPlayItem = updated;
        }

        MessageResponse messageResponse = new DataMessageResponse<>(
                messageType,
                "SYSTEM",
                msg,
                auctionPlayItem
                );


        channelService.publishMessageToChannel(channel.getId(), messageResponse);

        // 다음 매물로
        this.nextItem(channel.getId());

    }

    // TODO : 현재 '입찰 전 대기시간 타이머'에만 적용. 다른 타이머, 하이라이트등 채널 구성원의 완료 플래그 검사 진행 후 동작하는 기능에 적용해야함
    /**
     * 서버가 모종의 이유로 (타이머,하이라이트) 완료 메시지를 받지 못한 경우 실행
     * 메시지를 수신하지 못해도 제한 시간이 지나면, 경매가 진행되도록 하기위한 목적
     * @param channelId
     */
    public void executeChannelTimerEnd(long channelId) {

        Channel channel = channelService.findOne(channelId);

        scheduledExecutorService.schedule(() -> {
            List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));
            if(connections.stream().anyMatch(connection -> !connection.isCounted())) {

                MessageResponse messageResponse = new DataMessageResponse<>(
                        MessageType.COMPLETE_COUNT,
                        "SYSTEM",
                        MessageType.COMPLETE_COUNT.makeFullMessage(""),
                        MessageType.COMPLETE_COUNT);


                channelService.publishMessageToChannel(channelId, messageResponse);
            }
        }, channel.getWaitingTimeForAfterBid() + 2, TimeUnit.SECONDS);

    }


    private boolean isEveryoneOnChannelComplete(long channelId) {
        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        return connections.stream().allMatch(ChannelConnection::isCounted);
    }

    private void initCompletionFlagOnChannel(long channelId) {

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        for(ChannelConnection connection : connections) {
            connection.setCurrentHighlightCompleted(false);
            connection.setCounted(false);
        }

    }
}
