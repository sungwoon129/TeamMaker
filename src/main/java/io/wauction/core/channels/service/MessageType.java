package io.wauction.core.channels.service;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public enum MessageType {

    PRICE("price"),
    STATUS("status"),
    SYSTEM("system");

    private final String name;

    MessageType(String name) {
        this.name = name;
    }

}
