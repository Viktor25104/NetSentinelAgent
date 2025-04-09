package netsentinel.agent.dto.terminal;

/**
 * DTO-запрос для выполнения команды в терминале.
 * Используется в API или WebSocket при передаче команды от клиента на агент.
 *
 * @param command строка команды (например, {@code ls -la} или {@code tasklist})
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record CommandRequest(
        String command
) {}
