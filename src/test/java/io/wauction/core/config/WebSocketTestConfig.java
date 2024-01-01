package io.wauction.core.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

@SpringJUnitConfig
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class WebSocketTestConfig {

    @LocalServerPort
    protected int port;

    protected WebSocketStompClient stompClient;

    protected StompSession webSocketSession;

    protected String getWebSocketUri(String path) {
        return "ws://localhost:" + port + path;
    }

    protected void connectWebSocket(String path) throws Exception {
        stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        webSocketSession = stompClient.connect(getWebSocketUri(path), null).get();
    }

    protected void disconnectWebSocket() {
        if (webSocketSession != null && webSocketSession.isConnected()) {
            webSocketSession.disconnect();
        }
    }

}