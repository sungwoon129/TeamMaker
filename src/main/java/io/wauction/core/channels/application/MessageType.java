package io.wauction.core.channels.application;

import lombok.Getter;

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
