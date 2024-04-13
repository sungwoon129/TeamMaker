package io.wauction.core.channels.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import io.wauction.core.channels.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMessageResponse<T> extends MessageResponse {

    private T data;

    public DataMessageResponse(final MessageType messageType, String writer, String msg,final T data) {
        super(messageType, writer, msg);
        this.data = data;
    }
}
