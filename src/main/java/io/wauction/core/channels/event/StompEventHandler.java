package io.wauction.core.channels.event;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.config.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class StompEventHandler {

    // TODO : public으로 모든 클래스에서 접근하는 것이 아니라 접근 가능클래스를 제한해야할 필요 있음
    public static final Map<String, List<ChannelConnection>> subscribeMap = new ConcurrentHashMap<>();
    private final ChannelService channelService;


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String channelId = Objects.requireNonNull(headerAccessor.getNativeHeader("id")).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("접속한 채널 정보가 올바르지 않습니다."));
        String sender = Objects.requireNonNull(headerAccessor.getNativeHeader("user")).stream().findFirst().orElseThrow(() -> new IllegalArgumentException("접속한 채널 정보가 올바르지 않습니다."));

        List<ChannelConnection> connections = subscribeMap.getOrDefault(channelId, new ArrayList<>());

        String role = channelService.enter(Long.parseLong(channelId), sender);

        ChannelConnection channelConnection = new ChannelConnection(headerAccessor.getSessionId(), channelId, role);
        connections.add(channelConnection);

        headerAccessor.setUser(new CustomPrincipal(role, channelId));

        subscribeMap.put(channelId, connections);

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String channelId = headerAccessor.getSubscriptionId();

        if (channelId == null) {
            for (Map.Entry<String, List<ChannelConnection>> entry : subscribeMap.entrySet()) {
                for (ChannelConnection connection : entry.getValue()) {
                    if (connection.getSessionId().equals(headerAccessor.getSessionId())) {
                        channelId = entry.getKey();
                    }
                }
            }
        }

        List<ChannelConnection> connections = subscribeMap.get(channelId);
        Optional<ChannelConnection> disConnectSession = connections.stream().filter(connection -> connection.getSessionId().equals(headerAccessor.getSessionId())).findFirst();

        if (disConnectSession.isPresent()) {
            subscribeMap.put(channelId,
                    connections.stream().filter(connection -> !connection.getSessionId().equals(disConnectSession.get().getSessionId()))
                            .collect(Collectors.toList()));

            channelService.leave(Long.parseLong(channelId), disConnectSession.get().getRole(), subscribeMap.get(channelId));
        }


    }


}
