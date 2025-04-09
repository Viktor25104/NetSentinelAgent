package netsentinel.agent.service.terminal;

import netsentinel.agent.dto.terminal.CommandResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Сервис для удалённого выполнения команд в командной строке/терминале.
 * <p>
 * Поддерживает выполнение как в Windows, так и в Unix-подобных системах.
 * Возвращает стандартный вывод, ошибки и статус выполнения.
 *
 * Применяется в {@link netsentinel.agent.controller.api.TerminalApiController}.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class CommandExecutorService {

    /**
     * Выполняет указанную текстовую команду в системной оболочке.
     * <ul>
     *     <li>Для Windows: {@code cmd.exe /c}</li>
     *     <li>Для Linux/macOS: {@code /bin/sh -c}</li>
     * </ul>
     *
     * @param command строка с командой, например {@code "ping google.com"}
     * @return {@link CommandResponse} — содержит stdout, stderr и флаг успеха
     */
    public CommandResponse executeCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmd;

        if (os.contains("win")) {
            cmd = new String[]{"cmd.exe", "/c", command};
        } else {
            cmd = new String[]{"/bin/sh", "-c", command};
        }

        String output = "";
        String error = "";
        boolean success = false;

        try {
            Process process = new ProcessBuilder(cmd).start();

            output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            error = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();
            success = (exitCode == 0);

        } catch (Exception e) {
            error = e.getMessage();
        }

        return new CommandResponse(output, error, success);
    }
}
