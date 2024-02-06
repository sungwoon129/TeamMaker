package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MessageRequest {

    private String sender;
    private String type;
    private String message;

}
