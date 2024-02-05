package io.wauction.core.channels.entity;

import jakarta.persistence.*;

@Entity
public class ChannelMember extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long channelId;

    private Long memberId;

    @Column
    private Boolean deleted;
}
