package netsentinel.agent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Конфигурация WebSocket-сервера на стороне Spring Boot.
 * <p>
 * Регистрирует STOMP endpoint и настраивает брокер сообщений.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Настраивает адреса брокеров сообщений:
     * <ul>
     *   <li>/topic — рассылка</li>
     *   <li>/queue — точка-ответ</li>
     * </ul>
     *
     * @param config конфиг брокера
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Регистрирует точку входа для WebSocket/STOMP.
     *
     * @param registry реестр эндпоинтов
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
