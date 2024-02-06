package io.wauction.core.channels.entity;

import io.wauction.core.channels.dto.ChannelRequest;
import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
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
    private Boolean deleted;


    public static Channel createChannel(ChannelRequest channelRequest) {
        return Channel.builder()
                .name(channelRequest.getName())
                .capacity(channelRequest.getCapacity())
                .build();
    }


}
