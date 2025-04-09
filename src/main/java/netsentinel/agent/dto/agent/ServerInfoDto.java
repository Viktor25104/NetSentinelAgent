package netsentinel.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO, представляющий собой краткую информацию о сервере,
 * отправляемую на сервер при регистрации агента.
 *
 * Содержит технические и логические параметры:
 * IP, имя, ОС, статус, метрики и принадлежность к компании.
 *
 * Пример JSON:
 * <pre>
 * {
 *   "sessionId": "abc-123",
 *   "name": "Server-01",
 *   "ip": "192.168.1.100",
 *   "type": "Linux",
 *   "status": "online",
 *   "location": "DataCenter-1",
 *   "uptime": 86400,
 *   "cpuUsage": 47,
 *   "memoryUsage": 63,
 *   "diskUsage": 72,
 *   "companyId": 1
 * }
 * </pre>
 *
 * @param sessionId уникальный идентификатор WebSocket-сессии
 * @param name имя сервера
 * @param ip IP-адрес
 * @param type тип ОС (например, Linux, Windows)
 * @param status статус (online, offline и т.д.)
 * @param location физическое расположение
 * @param uptime время непрерывной работы в секундах
 * @param cpuUsage загрузка процессора (в процентах)
 * @param memoryUsage загрузка ОЗУ (в процентах)
 * @param diskUsage загрузка дисков (в процентах)
 * @param companyId идентификатор компании
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record ServerInfoDto(
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("name") String name,
        @JsonProperty("ip") String ip,
        @JsonProperty("type") String type,
        @JsonProperty("status") String status,
        @JsonProperty("location") String location,
        @JsonProperty("uptime") long uptime,
        @JsonProperty("cpuUsage") int cpuUsage,
        @JsonProperty("memoryUsage") int memoryUsage,
        @JsonProperty("diskUsage") int diskUsage,
        @JsonProperty("companyId") long companyId
) {}
