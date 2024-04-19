package io.wauction.core.auction.entity;

import io.wauction.core.auction.dto.AuctionItemResponse;
import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction_item")
public class AuctionItem {

    @Column(name = "item_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Comment("경매 대상의 담당 포지션(역할)")
    private String position;

    @Comment("경매 대상의 이미지 url")
    private String img;

    @Comment("사진이나 동영상등 경매상품의 가치를 표현할 수 있는 자료")
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "item_id", updatable = false, nullable = false)
    private List<Highlight> highlights = new ArrayList<>();

    public AuctionItemResponse toResponseDto() {
        return AuctionItemResponse.builder()
                .id(id)
                .name(name)
                .position(position)
                .img(img)
                .highlights(highlights.stream().map(Highlight::toResponseDto).toList())
                .build();

    }
}
