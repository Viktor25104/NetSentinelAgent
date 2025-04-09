package netsentinel.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Настройки WebSocket-агента, загружаемые из application.yml.
 * <p>
 * Используются для конфигурации подключения агента к серверу,
 * а также информации о самом агенте (имя, IP, тип, локация и т.д.).
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@ConfigurationProperties(prefix = "agent")
public record AgentProperties(
        String serverUrl,   // Адрес WebSocket-сервера
        String name,        // Имя сервера
        String ip,          // IP-адрес
        String type,        // Тип ОС (Linux, Windows)
        String location,    // Локация (датацентр, регион)
        long companyId      // Идентификатор компании
) {}
