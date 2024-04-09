package io.wauction.core.channels.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChannelConnection {
    private String sessionId;
    private String uid;
    private String channelId;
    private String role;
    private boolean isReady;


    public ChannelConnection(String sessionId, String channelId) {
        this.sessionId = sessionId;
        this.channelId = channelId;
    }

    public ChannelConnection(String sessionId, String uid, String channelId, String role) {
        this.sessionId = sessionId;
        this.uid = uid;
        this.channelId = channelId;
        this.role = role;
        this.isReady = false;
    }

}
