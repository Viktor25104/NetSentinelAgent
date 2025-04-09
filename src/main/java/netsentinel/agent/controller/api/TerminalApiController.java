package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.dto.terminal.CommandRequest;
import netsentinel.agent.dto.terminal.CommandResponse;
import netsentinel.agent.service.terminal.CommandExecutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API для выполнения команд в терминале.
 * Используется для удалённого доступа к shell.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terminal")
public class TerminalApiController {

    private final CommandExecutorService commandExecutorService;

    /**
     * Выполняет команду, полученную от клиента, и возвращает результат.
     *
     * @param request объект с командой
     * @return {@link CommandResponse} с результатом выполнения
     */
    @PostMapping("/execute")
    public ResponseEntity<CommandResponse> executeCommand(@RequestBody CommandRequest request) {
        return ResponseEntity.ok(commandExecutorService.executeCommand(request.command()));
    }
}
