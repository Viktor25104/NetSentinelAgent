package netsentinel.agent;

import netsentinel.agent.config.AgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Главный класс Spring Boot-приложения Net Sentinel Agent.
 * <p>
 * Выполняет запуск агента, активирует:
 * <ul>
 *     <li>{@code @SpringBootApplication} — автоконфигурацию Spring</li>
 *     <li>{@code @EnableScheduling} — поддержку планировщика задач</li>
 *     <li>{@code @EnableConfigurationProperties} — бин {@link AgentProperties}</li>
 * </ul>
 *
 * Точка входа в агент. После запуска:
 * <ul>
 *     <li>подключается WebSocket</li>
 *     <li>регистрируется сервер</li>
 *     <li>активируется периодический мониторинг</li>
 * </ul>
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AgentProperties.class)
public class AgentApplication {

    /**
     * Точка входа в приложение.
     *
     * @param args параметры командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
