package io.wauction.core.auction.entity;

import io.wauction.core.auction.dto.TeamPositionResponse;
import jakarta.persistence.*;
import org.hibernate.annotations.DynamicInsert;

@DynamicInsert
@Entity
public class TeamPosition {

    @Column(name = "position_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "position_name")
    private String name;


    public TeamPositionResponse toResponseDto() {
        return TeamPositionResponse.builder()
                .id(id)
                .name(name)
                .build();
    }
}
