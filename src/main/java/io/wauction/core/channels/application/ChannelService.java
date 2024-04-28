package io.wauction.core.channels.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.wauction.core.auction.dto.AuctionPlayItem;
import io.wauction.core.auction.dto.AuctionRuleResponse;
import io.wauction.core.auction.entity.ParticipantRole;
import io.wauction.core.auction.infrastructure.AuctionOrderRepository;
import io.wauction.core.channels.dto.*;
import io.wauction.core.channels.entity.Channel;
import io.wauction.core.channels.entity.ChannelState;
import io.wauction.core.channels.entity.MessageType;
import io.wauction.core.channels.infrastructure.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static io.wauction.core.auction.entity.AuctionOrder.createAuctionOrder;
import static io.wauction.core.channels.event.StompEventHandler.subscribeMap;


@Slf4j
@RequiredArgsConstructor
@Service
public class ChannelService {

    private final SimpMessageSendingOperations simpMessageSendingOperations;
    private final ChannelRepository channelRepository;
    private final AuctionOrderRepository auctionOrderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public List<Channel> findAll() {
        return channelRepository.findByDeletedIsNullOrDeletedFalse();
    }

    public Channel findOne(Long id) {
        return channelRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 ID 입니다."));
    }

    public void save(Channel channel) {
        channelRepository.save(channel);
    }

    @Transactional
    public String enter(long channelId, String sender) {
        Channel channel = findOne(channelId);
        channel.enter();

        ParticipantRole role = channel.getAuctionRule().getRoles().stream().filter(r -> r.getName().equals(sender)).findFirst().orElseThrow(() -> new NoSuchElementException("메시지 전송자를 유효한 역할에서 찾을 수 없습니다."));

        return role.getName();

    }

    public void leave(long channelId, String sender, List<ChannelConnection> connections) {
        Channel channel = findOne(channelId);
        channel.leave();

        List<String> activeRoles = connections.stream().map(ChannelConnection::getRole).toList();


        MessageResponse responseDto = EnterMessageResponse.builder()
                .messageType(MessageType.LEAVE)
                .writer("SYSTEM")
                .sender(sender)
                .activeRoles(activeRoles)
                .manager(connections.stream().filter(ChannelConnection::isManager).findAny().orElseThrow(() -> new NullPointerException("채널의 방장 정보를 찾을 수 없습니다")).getRole())
                .build();

        publishMessageToChannel(channel.getId(), responseDto);
    }

    public void countReady(long channelId, String sessionId, MessageRequest messageRequest, boolean isPlus) {

        Channel channel = this.findOne(channelId);

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        for(ChannelConnection channelConnection : connections) {
            if(channelConnection.getSessionId().equals(sessionId)) channelConnection.setReady(isPlus);
        }

        MessageType messageType = MessageType.findByTitle(messageRequest.getType());

        this.publishMessageToChannel(channelId, new ReadyMessageResponse(
                messageType,
                messageRequest.getSender(),
                messageType.makeFullMessage(messageRequest.getMessage()),
                (int) connections.stream().filter(ChannelConnection::isReady).count(),
                channel.getCapacity()));
    }


    public void requestForRoleExchange(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(
                MessageType.EXCHANGE,
                messageRequest.getSender(),
                messageType.makeFullMessage(messageRequest.getSender()),
                messageRequest.getTargetUsername());

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        Optional<ChannelConnection> connection = connections.stream().filter(connect -> connect.getRole().equals(messageRequest.getTargetUsername())).findAny();
        if(connection.isEmpty()) throw new IllegalArgumentException(messageRequest.getTargetUsername() + " 은(는) 올바른 메시지 수신자가 아닙니다.");

        publishMessageToUser(channelId, connection.get().getUid(), messageResponse);

    }

    public void responseRoleExchangeRequest(long channelId, MessageRequest messageRequest, MessageType messageType) {

        MessageResponse messageResponse = new MessageResponse(
                MessageType.EXCHANGE_RES,
                messageRequest.getSender(),
                messageType.makeFullMessage(messageRequest.getMessage()),
                messageRequest.getTargetUsername());

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        Optional<ChannelConnection> targetConnection = connections.stream().filter(connect -> connect.getRole().equals(messageRequest.getTargetUsername())).findAny();
        if(targetConnection.isEmpty()) throw new IllegalArgumentException(messageRequest.getTargetUsername() + " 은(는) 올바른 메시지 수신자가 아닙니다.");

        if (messageRequest.getMessage().equals("Y")) {

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.info("before swap = {} : {}", c.getRole(), c.getSessionId()));

            for (ChannelConnection connection : connections) {
                if (connection.getRole().equals(messageRequest.getTargetUsername())) {
                    connection.setRole(messageRequest.getSender());
                } else if (connection.getRole().equals(messageRequest.getSender())) {
                    connection.setRole(messageRequest.getTargetUsername());
                }
            }

            subscribeMap.put(String.valueOf(channelId), connections);

            subscribeMap.get(String.valueOf(channelId)).forEach(c -> log.info("after swap = {} : {}", c.getRole(), c.getSessionId()));
        }


        publishMessageToUser(channelId, targetConnection.get().getUid(), messageResponse);
    }

    @Transactional
    public void start(long channelId, String sessionId) {

        List<ChannelConnection> connections = subscribeMap.get(String.valueOf(channelId));

        // TODO : exception 발생 시 클라이언트 처리
        ChannelConnection client = connections.stream().filter(connect -> connect.getSessionId().equals(sessionId)).findAny().orElseThrow(() -> new IllegalArgumentException("권한이 없는 클라이언트의 요청입니다."));

        if(!client.isManager()) throw new IllegalArgumentException("권한이 없는 클라이언트의 요청입니다.");
        if(connections.stream().anyMatch(channelConnection -> !channelConnection.isReady())) throw new IllegalStateException("참가자 모두가 준비완료 상태여야 시작할 수 있습니다.");

        Channel channel = this.findOne(channelId);

        channel.changeState(ChannelState.PLAYING);


        AuctionRuleResponse auctionRuleResponse = channel.getAuctionRule().toResponseDto();

        AuctionStartData auctionStartData = AuctionStartData.builder()
                .positions(auctionRuleResponse.getPositions())
                .items(auctionRuleResponse.getItems())
                .roles(auctionRuleResponse.getRoles())
                .proceedWay(auctionRuleResponse.getProceedWay())
                .order(channel.getOrderNum())
                .waitingTimeForNext(channel.getWaitingTimeForNext())
                .waitingTimeForAfterBid(channel.getWaitingTimeForAfterBid())
                .build();

        // 경매순서 섞기
        auctionStartData.shuffleItems();

        // 섞인 경매순서 저장
        auctionOrderRepository.save(createAuctionOrder(channelId, auctionStartData.getItems().stream()
                        .map(item -> new AuctionPlayItem(item.getId(), item.getName()))
                        .toList()
                ));

        // 첫 번째 경매대상
        AuctionPlayItem auctionPlayItem = AuctionPlayItem.builder()
                .itemId(auctionStartData.getItems().get(0).getId())
                .name(auctionStartData.getItems().get(0).getName())
                .order(auctionStartData.getOrder())
                .price(0)
                .build();

        auctionStartData.setAuctionPlayItem(auctionPlayItem);

        MessageResponse messageResponse = new DataMessageResponse<>(MessageType.START, "SYSTEM", MessageType.START.makeFullMessage(""), auctionStartData);

        this.publishMessageToChannel(channelId, messageResponse);

    }

    public <T extends MessageResponse> void publishMessageToChannel(long channelId, T messageResponseDto) {
        String destination = "/channel/" + channelId;

        try {
            String resultMsg = objectMapper.writeValueAsString(messageResponseDto);
            simpMessageSendingOperations.convertAndSend(destination, resultMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public <T extends MessageResponse> void publishMessageToUser(long channelId, String targetUser, T messageResponseDto) {
        String destination = "/channel" +
                "/" +
                channelId +
                "/" +
                targetUser +
                "/secured";

        try {
            String resultMsg = objectMapper.writeValueAsString(messageResponseDto);
            simpMessageSendingOperations.convertAndSend(destination, resultMsg);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }



}
