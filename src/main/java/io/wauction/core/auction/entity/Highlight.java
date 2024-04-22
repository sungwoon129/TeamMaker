package io.wauction.core.auction.entity;

import io.wauction.core.auction.dto.HighlightResponse;
import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Highlight extends BaseTimeEntity {

    @Column(name = "highlight_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    private String name;

    private int length;

    @Lob
    private String url;

    public HighlightResponse toResponseDto() {
        return HighlightResponse.builder()
                .id(id)
                .name(name)
                .length(length)
                .url(url)
                .build();
    }
}
