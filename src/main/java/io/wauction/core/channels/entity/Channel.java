package io.wauction.core.channels.entity;

import io.wauction.core.auction.application.AuctionRuleService;
import io.wauction.core.auction.entity.AuctionRule;
import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.channels.dto.ChannelResponse;
import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

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
        return ChannelResponse.builder()
                .channelId(id)
                .name(name)
                .capacity(capacity)
                .auctionRuleResponse(auctionRule.toResponseDto())
                .build();
    }

    public void changeState(ChannelState state) {
        this.state = state;
    }
}
