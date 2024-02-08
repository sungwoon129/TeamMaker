package io.wauction.core.auction.entity;

import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class AuctionRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Long id;


    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "auction_role", joinColumns = @JoinColumn(name = "rule_id"))
    private List<ParticipantRole> roles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "auction_position", joinColumns = @JoinColumn(name = "rule_id"))
    private List<TeamPosition> positions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ProceedWay proceedWay;


}
