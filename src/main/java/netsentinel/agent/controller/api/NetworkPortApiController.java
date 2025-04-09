package netsentinel.agent.controller.api;

import netsentinel.agent.dto.network.NetworkPortDto;
import netsentinel.agent.service.network.NetworkPortMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API-контроллер для получения информации об открытых сетевых портах.
 * Возвращает актуальный список прослушиваемых портов и связанных процессов.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/monitor")
public class NetworkPortApiController {

    private final NetworkPortMonitoringService service;

    public NetworkPortApiController(NetworkPortMonitoringService service) {
        this.service = service;
    }

    /**
     * Возвращает список открытых портов, включая информацию о протоколе, процессе и статусе.
     *
     * @return список {@link NetworkPortDto}
     */
    @GetMapping("/ports")
    public ResponseEntity<List<NetworkPortDto>> getPorts() {
        return ResponseEntity.ok(service.getPorts());
    }
}
