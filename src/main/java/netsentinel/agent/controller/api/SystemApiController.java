package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.dto.system.CpuInfoDto;
import netsentinel.agent.service.system.CpuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemApiController {

    private final CpuService cpuService;

    @GetMapping("/cpu")
    public ResponseEntity<CpuInfoDto> getCpuInfo() {
        return ResponseEntity.ok(cpuService.getCpuInfo());
    }
}
