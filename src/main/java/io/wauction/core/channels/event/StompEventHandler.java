package io.wauction.core.channels.event;

import io.wauction.core.channels.application.ChannelService;
import io.wauction.core.channels.dto.ChannelConnection;
import io.wauction.core.channels.dto.EnterMessageResponse;
import io.wauction.core.channels.dto.MessageResponse;
import io.wauction.core.channels.entity.MessageType;
import io.wauction.core.config.CustomPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Component
public class StompEventHandler {

    // TODO : public으로 모든 클래스에서 접근하는 것이 아니라 접근 가능클래스를 제한해야할 필요 있음 (자리교환 기능과 같이 map에 변경이 필요한 경우 event로 처리하는것이 좋아보임 )
    public static final Map<String, List<ChannelConnection>> subscribeMap = new ConcurrentHashMap<>();
    private final ChannelService channelService;


    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        CustomPrincipal user = (CustomPrincipal) event.getUser();

        assert user != null;

        String channelId = user.getChannelId();
        String sender = user.getRole();

        List<ChannelConnection> connections = subscribeMap.getOrDefault(channelId, new ArrayList<>());

        String role = channelService.enter(Long.parseLong(channelId), sender);

        boolean isManager = connections.isEmpty();

        ChannelConnection channelConnection = new ChannelConnection(headerAccessor.getSessionId(), user.getName(), channelId, role, isManager);
        connections.add(channelConnection);

        subscribeMap.put(channelId, connections);

    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if(!Objects.requireNonNull(headerAccessor.getDestination()).contains("secured")) {

            String channelId = extractChannelId(headerAccessor.getSubscriptionId());

            if(channelId == null) throw new NullPointerException("메시지 헤더 정보가 올바르지 않습니다.");

            List<ChannelConnection> connections = subscribeMap.get(channelId);
            ChannelConnection channelConnection = connections.stream().filter(connection -> connection.getSessionId().equals(headerAccessor.getSessionId())).findFirst().orElseThrow(() -> new IllegalArgumentException("클라이언트의 세션 정보를 찾을 수 없습니다."));


            MessageResponse responseDto = new EnterMessageResponse(
                    MessageType.JOIN,
                    "SYSTEM",
                    channelConnection.getRole().toUpperCase(),
                    MessageType.JOIN.makeFullMessage(channelConnection.getRole()),
                    connections.stream().filter(ChannelConnection::isManager).findAny().orElseThrow(() -> new NullPointerException("채널의 방장 정보를 찾을 수 없습니다")).getRole());

            channelService.publishMessageToChannel(Long.parseLong(channelId), responseDto);

        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String channelId = findChannelIdFromMap(event, headerAccessor);

        // TODO Map에서 get한 객체 리스트의 원소를 수정하면 Map에 들어있는 데이터도 수정되는지 확인
        List<ChannelConnection> connections = subscribeMap.get(channelId);
        Optional<ChannelConnection> disConnectSession = connections.stream().filter(connection -> connection.getSessionId().equals(headerAccessor.getSessionId())).findFirst();

        if (disConnectSession.isPresent()) {
            List<ChannelConnection> updated =  connections.stream().filter(connection -> !connection.getSessionId().equals(disConnectSession.get().getSessionId()))
                    .toList();

            if(disConnectSession.get().isManager()) {
                updated.get(0).setManager(true);
            }

            subscribeMap.put(channelId, updated);


            channelService.leave(Long.parseLong(channelId), disConnectSession.get().getRole(), updated);
        }

    }

    private String findChannelIdFromMap(SessionDisconnectEvent event, StompHeaderAccessor headerAccessor) {
        CustomPrincipal user = (CustomPrincipal) event.getUser();

        assert user != null;
        String channelId = user.getChannelId();

        // 나가기 버튼을 이용하지 않고 브라우저 종료, 새로고침등의 이유로 disconnect 된 경우, connectionMap을 순회하면서 session 조회해 id 찾기
        if (channelId == null) {
            for (Map.Entry<String, List<ChannelConnection>> entry : subscribeMap.entrySet()) {
                for (ChannelConnection connection : entry.getValue()) {
                    if (connection.getSessionId().equals(headerAccessor.getSessionId())) {
                        channelId = entry.getKey();
                    }
                }
            }
        }
        return channelId;
    }

    private String extractChannelId(String subId) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(subId);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }


}
