package io.wauction.core.channels.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChannelConnection {
    private String sessionId;
    private String channelId;
    private String role;


    public ChannelConnection(String sessionId, String channelId) {
        this.sessionId = sessionId;
        this.channelId = channelId;
    }

}
