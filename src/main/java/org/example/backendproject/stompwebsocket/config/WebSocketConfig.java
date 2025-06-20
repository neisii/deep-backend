package org.example.backendproject.stompwebsocket.config;

import org.example.backendproject.stompwebsocket.handler.CustomHandShakeHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); // sub server to client

        registry.setApplicationDestinationPrefixes("/app"); // pub client to server
        registry.setUserDestinationPrefix("/user"); // pub client to server
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 전체 채팅
        registry.addEndpoint("/ws-chat")
                .setHandshakeHandler(new CustomHandShakeHandler())
                .setAllowedOriginPatterns("*");

        // gpt 채팅
        registry.addEndpoint("/ws-gpt")
                .setAllowedOriginPatterns("*");
    }
}
