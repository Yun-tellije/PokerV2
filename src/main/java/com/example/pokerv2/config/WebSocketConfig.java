package com.example.pokerv2.config;

import com.example.pokerv2.stomp.StompHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompHandler stompHandler;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
        // 웹소켓은 URL이 http가 아닌 ws가 사용됨
        // setAllowedOriginPatterns("*") -> CORS 설정을 모두 허용
        // .withSockJS(); -> 웹소켓을 지원하지 않는 브라우저는 sockJS를 사용(근데 테스트 시 오류 발생)
    }

    @Override
    // 메시지 브로커 설정(발신자의 메시지를 받아와 수신자들에게 메시지 전달)
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/queue", "/topic");
        registry.setApplicationDestinationPrefixes("/pub");
        // 스프링에서 제공하는 내장 브로커 사용 | queue(1:1), topic(1:n)
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(stompHandler);
    }
}