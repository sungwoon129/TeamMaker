package io.wauction.core.channels.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {

    private String sender;
    private String type;
    private String message;
    private String targetUsername;


    public String getSender() {

        if(sender == null || sender.isEmpty()) {
            return "SYSTEM";
        }

        return sender;
    }

}
