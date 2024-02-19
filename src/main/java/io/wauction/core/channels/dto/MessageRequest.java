package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;


@NoArgsConstructor
public class MessageRequest {

    private String sender;
    private String type;
    private String message;


    public String getSender() {

        if(sender == null || sender.isEmpty()) {
            return "SYSTEM";
        }

        return sender;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
