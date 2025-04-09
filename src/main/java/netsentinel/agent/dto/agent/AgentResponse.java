package netsentinel.agent.dto.agent;

/**
 * DTO-обёртка для WebSocket-ответа агента.
 * <p>
 * Используется для отправки любых данных с типом и sessionId
 * обратно на сервер через STOMP. Поддерживает универсальный {@code payload},
 * который может быть DTO любого типа.
 *
 * Пример JSON:
 * <pre>
 * {
 *   "sessionId": "abc123",
 *   "type": "cpu",
 *   "payload": {
 *     "name": "Intel Xeon",
 *     "load": 64.5
 *   }
 * }
 * </pre>
 *
 * @param sessionId идентификатор текущей WebSocket-сессии
 * @param type тип запроса/ответа (например: cpu, ram, info)
 * @param payload любые сериализуемые данные (DTO)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record AgentResponse(
        String sessionId,
        String type,
        Object payload
) {
}
