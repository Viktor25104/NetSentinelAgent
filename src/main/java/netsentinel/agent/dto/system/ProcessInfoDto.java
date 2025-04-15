package netsentinel.agent.dto.system;

/**
 * DTO, описывающее отдельный процесс в системе.
 * Поддерживает вывод как для Windows, так и для Linux.
 *
 * @param pid идентификатор процесса (PID)
 * @param name имя исполняемого файла
 * @param sessionName имя сессии (только Windows)
 * @param sessionId идентификатор сессии (Windows)
 * @param memoryUsage использование памяти в виде строки (Windows)
 * @param mem использование памяти в % (Linux)
 * @param cpu использование CPU в % (Linux)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record ProcessInfoDto(
        int pid,
        String name,
        String sessionName,
        Integer sessionId,
        Long memoryUsage,
        Double mem,
        Double cpu
) {}
