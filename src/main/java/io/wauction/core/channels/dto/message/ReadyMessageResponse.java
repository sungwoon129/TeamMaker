package io.wauction.core.channels.dto.message;

import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadyMessageResponse extends MessageResponse {
    private int readyCount;
    private int capacity;

    public ReadyMessageResponse(MessageType messageType, String sender, String msg, int count, int capacity) {
        super(messageType, sender, msg);
        this.readyCount = count;
        this.capacity = capacity;
    }
}
