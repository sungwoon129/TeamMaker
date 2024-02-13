package io.wauction.core.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ParticipantRoleResponse {
    private Long id;
    private String name;
    private Long point;
}
