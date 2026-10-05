package co.edu.unisimon.corenotificacion.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import co.edu.unisimon.corenotificacion.security.WebSocketChannelInterceptor;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;

    @Value("${spring.websocket.stomp.relay.host:192.168.3.83}")
    private String relayHost;

    @Value("${spring.websocket.stomp.relay.port:61613}")
    private int relayPort;

    @Value("${spring.websocket.stomp.relay.system-login:guest}")
    private String systemLogin;

    @Value("${spring.websocket.stomp.relay.system-passcode:guest}")
    private String systemPasscode;

    @Value("${spring.websocket.stomp.relay.client-login:guest}")
    private String clientLogin;

    @Value("${spring.websocket.stomp.relay.client-passcode:guest}")
    private String clientPasscode;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor);
    }
}
