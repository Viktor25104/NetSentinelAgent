package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.dto.terminal.CommandRequest;
import netsentinel.agent.dto.terminal.CommandResponse;
import netsentinel.agent.service.terminal.CommandExecutorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/terminal")
public class TerminalApiController {

    private final CommandExecutorService commandExecutorService;

    @PostMapping("/execute")
    public CommandResponse executeCommand(@RequestBody CommandRequest request) {
        return commandExecutorService.executeCommand(request.command());
    }
}
