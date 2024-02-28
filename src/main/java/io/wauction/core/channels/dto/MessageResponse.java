package io.wauction.core.channels.dto;

import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private MessageType messageType;
    private String writer;
    private String msg;
}
