package io.wauction.core.channels.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChannelConnection {
    private String sessionId;
    private String channelId;
    private String role;

}
