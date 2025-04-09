package netsentinel.agent.dto.network;

/**
 * DTO, описывающее состояние прослушиваемого сетевого порта.
 * <p>
 * Используется в мониторинге портов, получаемых через системные команды netstat/lsof.
 *
 * @param port номер порта (0–65535)
 * @param protocol TCP/UDP
 * @param service имя процесса/службы, связанного с портом
 * @param state состояние соединения (LISTENING, ESTABLISHED и т.д.)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record NetworkPortDto(
        int port,
        String protocol,
        String service,
        String state
) {}
