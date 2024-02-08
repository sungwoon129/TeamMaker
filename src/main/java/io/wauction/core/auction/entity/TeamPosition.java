package io.wauction.core.auction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class TeamPosition {

    @Column(name = "position_name")
    private String name;
}
