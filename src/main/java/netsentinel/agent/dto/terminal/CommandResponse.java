package netsentinel.agent.dto.terminal;

/**
 * DTO-ответ с результатом выполнения команды в терминале.
 *
 * @param output стандартный вывод команды (stdout)
 * @param error стандартная ошибка (stderr)
 * @param success флаг успешности выполнения (true, если завершилась с кодом 0)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record CommandResponse(
        String output,
        String error,
        boolean success
) {}
