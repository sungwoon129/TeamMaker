package io.wauction.core.auction.entity;

import jakarta.persistence.*;

@Entity
public class ParticipantRole {

    @Column(name = "participant_role_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name")
    private String name;

    @Embedded
    private Point point;
}
