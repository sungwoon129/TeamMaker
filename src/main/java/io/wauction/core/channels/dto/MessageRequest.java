package io.wauction.core.channels.dto;

import io.wauction.core.channels.service.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageRequest {

    private Long channelId;
    private Long senderId;
    private MessageType type;
    private String message;


}
