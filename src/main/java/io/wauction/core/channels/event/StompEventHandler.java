package io.wauction.core.channels.event;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.config.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class StompEventHandler {

    private static final Map<String, List<ChannelConnection>> subscribeMap = new ConcurrentHashMap<>();
    private final ChannelService channelService;


    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String channelId = headerAccessor.getSubscriptionId();
        assert channelId != null;


        List<ChannelConnection> connections = subscribeMap.getOrDefault(channelId, new ArrayList<>());
        String role = channelService.enter(Long.parseLong(channelId), connections);


        ChannelConnection channelConnection = new ChannelConnection(headerAccessor.getSessionId(),channelId,role);
        headerAccessor.setUser(new CustomPrincipal(channelId + "/" + role));
        connections.add(channelConnection);

        subscribeMap.put(channelId, connections);

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String channelId = headerAccessor.getSubscriptionId();

        if(channelId == null) {
            for(Map.Entry<String, List<ChannelConnection>> entry : subscribeMap.entrySet()) {
                for(ChannelConnection connection : entry.getValue()) {
                    if (connection.getSessionId().equals(headerAccessor.getSessionId())) {
                        channelId = entry.getKey();
                    }
                }
            }
        }

        List<ChannelConnection> connections = subscribeMap.get(channelId);
        Optional<ChannelConnection> disConnectSession = connections.stream().filter(connection -> connection.getSessionId().equals(headerAccessor.getSessionId())).findFirst();

        if(disConnectSession.isPresent()) {
            subscribeMap.put(channelId,
                    connections.stream().filter(connection -> !connection.getSessionId().equals(disConnectSession.get().getSessionId()))
                            .collect(Collectors.toList()));
            channelService.leave(Long.parseLong(channelId),disConnectSession.get().getRole(),subscribeMap.get(channelId));
        }





    }



}
