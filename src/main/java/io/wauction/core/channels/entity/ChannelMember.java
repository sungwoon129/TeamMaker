package io.wauction.core.channels.entity;

import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class ChannelMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long channelId;

    private Long memberId;

    @Column
    private Boolean deleted;
}
