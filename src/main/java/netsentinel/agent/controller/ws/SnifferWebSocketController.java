package netsentinel.agent.controller.ws;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class SnifferWebSocketController {

    private final SnifferService snifferService;

    @MessageMapping("/capture/command")
    @SendTo("/topic/status")
    public String processCommand(String command) {
        return switch (command) {
            case "start" -> {
                snifferService.startCapture(null);
                yield "Capturing started";
            }
            case "stop" -> {
                snifferService.stopCapture();
                yield "Capturing stopped";
            }
            case "clear" -> {
                snifferService.clearCapture();
                yield "Capture cleared";
            }
            default -> "Unknown command";
        };
    }
}
