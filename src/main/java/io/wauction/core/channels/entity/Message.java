package io.wauction.core.channels.entity;

import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String content;

    private Long channelId;
    private String writer;

    @Column
    private Boolean deleted;
}
