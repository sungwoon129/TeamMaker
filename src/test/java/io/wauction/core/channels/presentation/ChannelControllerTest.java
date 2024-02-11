package io.wauction.core.channels.presentation;

import io.wauction.core.channels.dto.MessageRequest;
import io.wauction.core.config.WebSocketTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ChannelControllerTest extends WebSocketTestConfig {

    @Autowired
    private WebSocketStompClient stompClient;

    private StompSession stompSession;

    @BeforeEach
    void setUp() throws Exception {
        connectWebSocket("/websocket-endpoint");
    }

    @AfterEach
    void tearDown() {
        disconnectWebSocket();
    }

    @Test
    void testChatAndBidEndpoints() throws InterruptedException {

        CountDownLatch bidLatch = new CountDownLatch(1);
        stompSession.subscribe("/subscription/channels/1", new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {

                bidLatch.countDown();
            }
        });

        stompSession.send("/app/channels/1/bid", new MessageRequest());

        assertTrue(bidLatch.await(10, TimeUnit.SECONDS));
    }
}