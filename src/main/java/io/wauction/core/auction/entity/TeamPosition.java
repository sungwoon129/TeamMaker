package io.wauction.core.auction.entity;

import jakarta.persistence.*;

@Entity
public class TeamPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "position_name")
    private String name;
}
