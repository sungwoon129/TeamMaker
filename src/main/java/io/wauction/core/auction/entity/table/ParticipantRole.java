package io.wauction.core.auction.entity.table;

import io.wauction.core.auction.dto.ParticipantRoleResponse;
import io.wauction.core.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ParticipantRole extends BaseTimeEntity {

    @Column(name = "participant_role_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_name")
    private String name;

    @Embedded
    private Point point;

    public ParticipantRoleResponse toResponseDto() {
        return ParticipantRoleResponse.builder()
                .id(id)
                .name(name)
                .point(point.getValue())
                .build();
    }
}
