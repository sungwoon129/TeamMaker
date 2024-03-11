package io.wauction.core.config;

import io.wauction.core.channels.interceptor.StompInboundInterceptor;
import io.wauction.core.channels.interceptor.StompOutboundInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@RequiredArgsConstructor
@EnableWebSocketMessageBroker
@Configuration
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompInboundInterceptor inboundInterceptor;
    private final StompOutboundInterceptor outboundInterceptor;




    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/wauction")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new CustomHandShakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/channel", "/user");
        registry.setApplicationDestinationPrefixes("/wauction");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(inboundInterceptor);
        registration.interceptors(outboundInterceptor);
    }

}
