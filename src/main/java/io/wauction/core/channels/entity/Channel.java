package io.wauction.core.channels.entity;

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

    @OneToOne
    @Column
    private AuctionRule auctionRule;

    @Column
    private Boolean deleted;


    public static Channel createChannel(ChannelRequest channelRequest) {
        return Channel.builder()
                .name(channelRequest.getName())
                .capacity(channelRequest.getCapacity())
                .state(ChannelState.WAITING)
                .build();
    }


    public ChannelResponse toDto() {
        return ChannelResponse.builder()
                .channelId(id)
                .name(name)
                .capacity(capacity)
                .build();
    }

    public void changeState(ChannelState state) {
        this.state = state;
    }
}
