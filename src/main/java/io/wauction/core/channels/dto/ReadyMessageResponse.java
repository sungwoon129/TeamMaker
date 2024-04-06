package io.wauction.core.channels.dto;

import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadyMessageResponse {
    private MessageType messageType;
    private String writer;
    private String msg;
    private int readyCount;
    private int capacity;
}
