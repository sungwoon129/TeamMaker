package io.wauction.core.channels.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;



@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnterMessageResponse {
    private MessageType messageType;
    private String writer;
    private String sender;
    private String msg;
    private List<String> activeRoles;
    private String manager;

}
