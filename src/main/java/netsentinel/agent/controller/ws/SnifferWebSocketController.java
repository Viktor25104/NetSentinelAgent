package netsentinel.agent.controller.ws;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket-контроллер для управления сетевым сниффером через STOMP.
 * <p>
 * Обрабатывает команды из WebSocket-сообщений, такие как запуск,
 * остановка и очистка сниффера. Возвращает статусные сообщения клиенту.
 * Использует адреса:
 * <ul>
 *     <li><b>Входящий:</b> /app/capture/command</li>
 *     <li><b>Исходящий:</b> /topic/status</li>
 * </ul>
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Controller
@RequiredArgsConstructor
public class SnifferWebSocketController {

    private final SnifferService snifferService;

    /**
     * Обрабатывает команду сниффера, полученную через WebSocket.
     * <p>
     * Поддерживает команды:
     * <ul>
     *   <li>{@code "start"} — запускает захват</li>
     *   <li>{@code "stop"} — останавливает захват</li>
     *   <li>{@code "clear"} — очищает буфер</li>
     * </ul>
     *
     * @param command строковая команда
     * @return статус выполнения (текстовое сообщение)
     */
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
