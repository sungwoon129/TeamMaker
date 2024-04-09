package io.wauction.core.channels.entity;

import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.auction.entity.AuctionRule;
import io.wauction.core.auction.entity.ParticipantRole;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.dto.ChannelResponse;
import io.wauction.core.channels.exception.UnAcceptableChannelJoinException;
import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.wauction.core.channels.event.StompEventHandler.subscribeMap;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Channel extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long id;

    @Column
    private String name;

    @Column
    private int capacity;

    @Column(name = "ready_count")
    private int readyCount;

    @Column
    private ChannelState state;

    @ManyToOne
    private AuctionRule auctionRule;

    @Column
    private Boolean deleted;


    public static Channel createChannel(ChannelRequest channelRequest, AuctionRuleService auctionRuleService) {

        AuctionRule auctionRule = channelRequest.getAuctionRuleId() == 0L ? auctionRuleService.getDefaultRule() : auctionRuleService.findById(channelRequest.getAuctionRuleId());

        return Channel.builder()
                .name(channelRequest.getName())
                .capacity(auctionRule.getMaximumParticipants())
                .auctionRule(auctionRule)
                .state(ChannelState.WAITING)
                .build();
    }


    public ChannelResponse toResponseDto() {

        List<ChannelConnection> connections = subscribeMap.getOrDefault(id.toString(), new ArrayList<>());

        List<String> roles = auctionRule.getRoles().stream().map(ParticipantRole::getName).collect(Collectors.toList());
        List<String> activeRoles = connections.stream()
                .map(ChannelConnection::getRole)
                .filter(Objects::nonNull)
                .toList();

        roles.removeAll(activeRoles);

        String emptyRole = roles.get(0);

        Optional<ParticipantRole> role = auctionRule.getRoles().stream().filter(r -> r.getName().equals(emptyRole)).findFirst();

        if(role.isEmpty()) {
            throw new IndexOutOfBoundsException("현재 접속한 클라이언트에게 부여할 역할을 찾을 수 없습니다.");
        }

        return ChannelResponse.builder()
                .channelId(id)
                .name(name)
                .headCount(getHeadCount())
                .capacity(capacity)
                .auctionRuleResponse(auctionRule.toResponseDto())
                .clientRole(role.get().toResponseDto())
                .activeRoles(activeRoles)
                .readyRoles(connections.stream().filter(ChannelConnection::isReady).map(ChannelConnection::getRole).toList())
                .build();
    }

    public void changeState(ChannelState state) {
        this.state = state;
    }

    public void enter() {
        if(!isAdmissionStatus()) {
            throw new UnAcceptableChannelJoinException("해당 채널은 현재 입장할 수 없는 상태입니다.");
        }

        if(capacity == getHeadCount()) this.state = ChannelState.FULL;
    }

    private boolean isAdmissionStatus() {
        return (capacity > getHeadCount()) && state.equals(ChannelState.WAITING);
    }

    public void leave() {

        if(this.state != ChannelState.PLAYING) this.state = ChannelState.WAITING;
        if(subscribeMap.get(String.valueOf(this.id)).isEmpty() || getHeadCount() == 0) {
            this.state = ChannelState.END;
            this.deleted = true;
        }
    }

    private int getHeadCount() {
        return subscribeMap.get(String.valueOf(this.id)) == null ? 0 : subscribeMap.get(String.valueOf(this.id)).size();
    }

}
