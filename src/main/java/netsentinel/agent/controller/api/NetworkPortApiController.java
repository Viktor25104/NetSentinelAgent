package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.model.NetworkPort;
import netsentinel.agent.service.network.NetworkPortMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class NetworkPortApiController {

    private final NetworkPortMonitoringService networkPortMonitoringService;

    @GetMapping("/ports")
    public ResponseEntity<List<NetworkPort>> getNetworkPorts() {
        return ResponseEntity.ok(networkPortMonitoringService.getPorts());
    }
}
