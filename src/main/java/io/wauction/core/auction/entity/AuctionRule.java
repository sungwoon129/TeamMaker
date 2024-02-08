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


    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rule_id", updatable = false, nullable = false)
    private List<ParticipantRole> roles = new ArrayList<>();


    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rule_id", updatable = false, nullable = false)
    private List<TeamPosition> positions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "rule_id", updatable = false, nullable = false)
    private List<AuctionItem> auctionItems;

    @Enumerated(EnumType.STRING)
    private ProceedWay proceedWay;

    private int skeleton;


    // TODO: AuctionRule 생성할 때, auctionItems.size() / roles.size() 값은 1이상이어야함. 최소 1명은 되어야하기때문.
    public int getMaximumParticipants() {
        return auctionItems.size() / roles.size();
    }

}
