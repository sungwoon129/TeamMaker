package io.wauction.core.auction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class ParticipantRole {

    @Column(name = "role_name")
    private String name;

    @Embedded
    private Point point;
}
