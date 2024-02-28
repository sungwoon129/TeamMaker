package io.wauction.core.channels.dto;

import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EnterMessageResponse {
    private MessageType messageType;
    private String writer;
    private String msg;
    private int headCount;

}
