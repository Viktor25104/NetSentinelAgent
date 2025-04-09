package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.dto.network.PacketDto;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API-контроллер для управления сетевым сниффером пакетов.
 * Позволяет запускать и останавливать захват трафика, очищать буфер
 * и просматривать список захваченных пакетов в виде DTO.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sniffer")
public class SnifferApiController {

    private final SnifferService snifferService;

    /**
     * Возвращает список последних захваченных сетевых пакетов.
     *
     * @return список {@link PacketDto}
     */
    @GetMapping("/packets")
    public ResponseEntity<List<PacketDto>> getPackets() {
        return ResponseEntity.ok(snifferService.getCapturedPackets());
    }

    /**
     * Запускает захват трафика на выбранном интерфейсе.
     *
     * @param interfaceName имя интерфейса (eth0, wlan0 и т.д.)
     * @return сообщение об успехе
     */
    @PostMapping("/capture/start")
    public ResponseEntity<String> start(@RequestParam String interfaceName) {
        snifferService.startCapture(interfaceName);
        return ResponseEntity.ok("Сниффер запущен на интерфейсе: " + interfaceName);
    }

    /**
     * Останавливает захват сетевых пакетов.
     *
     * @return сообщение об успехе
     */
    @PostMapping("/capture/stop")
    public ResponseEntity<String> stop() {
        snifferService.stopCapture();
        return ResponseEntity.ok("Сниффер остановлен");
    }

    /**
     * Очищает список захваченных пакетов.
     *
     * @return сообщение об успехе
     */
    @PostMapping("/capture/clear")
    public ResponseEntity<String> clearCapture() {
        snifferService.clearCapture();
        return ResponseEntity.ok("Буфер сниффера очищен");
    }
}
