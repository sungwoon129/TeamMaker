package io.wauction.core.channels.interceptor;

import io.wauction.core.config.CustomPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class StompInboundInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        assert accessor != null;
        // StompHeaderAccessor 클래스에 user를 설정하는 위치가 preSend인 이유는 ChannelInterceptor는 security context에서 관리하는 영역이기 때문
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String channelId = Objects.requireNonNull(accessor.getNativeHeader("id")).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("접속한 채널 정보가 올바르지 않습니다."));
            String name = Objects.requireNonNull(accessor.getNativeHeader("user")).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("메시지 헤더 정보가 올바르지 않습니다."));
            String role = Objects.requireNonNull(accessor.getNativeHeader("role")).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("메시지 헤더 정보가 올바르지 않습니다."));

            if (channelId != null) {
                accessor.setUser(new CustomPrincipal(name, channelId, role));
            }
        }

        return message;
    }

}
