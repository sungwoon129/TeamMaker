package io.wauction.core.channels.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private MessageType messageType;
    private String writer;
    private String msg;
    private String targetUsername;
    private String resultYne;


    public MessageResponse(MessageType messageType, String writer, String msg) {
        this.messageType = messageType;
        this.writer = writer;
        this.msg = msg;
    }

    public MessageResponse(MessageType messageType, String writer, String msg, String targetUsername) {
        this.messageType = messageType;
        this.writer = writer;
        this.msg = msg;
        this.targetUsername = targetUsername;
    }

}
