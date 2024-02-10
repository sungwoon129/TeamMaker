package io.wauction.core.auction.entity;

import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;

@Entity
public class Highlight extends BaseTimeEntity {

    @Column(name = "highlight_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;
    private String url;
}
