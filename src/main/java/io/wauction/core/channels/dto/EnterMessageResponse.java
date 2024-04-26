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
public class EnterMessageResponse extends MessageResponse {
    private List<String> activeRoles;
    private String manager;


    public EnterMessageResponse(MessageType messageType, String writer, String sender, String msg, List<String> activeRoles, String manager) {
        super(messageType, writer, sender, msg);
        this.activeRoles = activeRoles;
        this.manager = manager;
    }

    public EnterMessageResponse(MessageType messageType, String writer, String sender, String msg, String manager) {
        super(messageType, writer, msg, sender);
        this.manager = manager;
    }

    public EnterMessageResponse(MessageType messageType, String writer, String sender, List<String> activeRoles, String manager) {
        super(messageType, writer, sender);
        this.activeRoles = activeRoles;
    }

}
