package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.system.CpuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemApiController {

    private final CpuService cpuService;

    @GetMapping("/cpu")
    public ResponseEntity<Map<String, Object>> getCpuInfo() {
        return ResponseEntity.ok(cpuService.getCpuInfo());
    }
}
