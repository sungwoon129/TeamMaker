package io.wauction.core.channels.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private String writer;
    private String msg;
}
