package netsentinel.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.List;

/**
 * Конфигурация STOMP WebSocket клиента.
 * <p>
 * Создаёт бин {@link WebSocketStompClient} с поддержкой
 * JSON и текстовых сообщений.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Configuration
public class WebSocketClientConfig {

    /**
     * Бин клиента STOMP, использующего {@link StandardWebSocketClient}.
     *
     * @return готовый {@link WebSocketStompClient}
     */
    @Bean
    public WebSocketStompClient webSocketStompClient() {
        var stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new CompositeMessageConverter(List.of(
                new StringMessageConverter(),
                new MappingJackson2MessageConverter()
        )));
        return stompClient;
    }
}
