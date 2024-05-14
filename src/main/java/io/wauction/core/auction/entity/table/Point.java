package io.wauction.core.auction.entity.table;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Embeddable
public class Point {

    @Column(name = "point")
    private long value;
}
