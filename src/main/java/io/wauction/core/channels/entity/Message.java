package io.wauction.core.channels.entity;

import jakarta.persistence.*;

@Entity
public class Message extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String content;

    private Long channelId;
    private Long memberId;

    @Column(columnDefinition = "TINYINT(1)")
    private char deleted;
}
