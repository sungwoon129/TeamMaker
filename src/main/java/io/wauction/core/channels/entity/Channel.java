package io.wauction.core.channels.entity;

import jakarta.persistence.*;

@Entity
public class Channel extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long id;

    @Column(columnDefinition = "TINYINT(1)")
    private char deleted;



}
