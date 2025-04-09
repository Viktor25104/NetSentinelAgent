package netsentinel.agent.controller.api;

import netsentinel.agent.dto.system.CpuInfoDto;
import netsentinel.agent.dto.system.RamInfoDto;
import netsentinel.agent.service.system.CpuService;
import netsentinel.agent.service.system.DiskService;
import netsentinel.agent.service.system.RamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API-контроллер для запроса системной информации сервера.
 * Позволяет получать метрики CPU, RAM и дисков.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/system")
public class SystemApiController {

    private final CpuService cpuService;
    private final RamService ramService;
    private final DiskService diskService;

    public SystemApiController(CpuService cpuService, RamService ramService, DiskService diskService) {
        this.cpuService = cpuService;
        this.ramService = ramService;
        this.diskService = diskService;
    }

    /**
     * Возвращает информацию о CPU (нагрузка, ядра, модель).
     *
     * @return {@link CpuInfoDto}
     */
    @GetMapping("/cpu")
    public ResponseEntity<CpuInfoDto> getCpu() {
        return ResponseEntity.ok(cpuService.getCpuInfo());
    }

    /**
     * Возвращает информацию о памяти RAM (свободно, использовано, загрузка).
     *
     * @return {@link RamInfoDto}
     */
    @GetMapping("/ram")
    public ResponseEntity<RamInfoDto> getRam() {
        return ResponseEntity.ok(ramService.getRamInfo());
    }

    /**
     * Возвращает общий уровень загрузки дисков (в процентах).
     *
     * @return int от 0 до 100
     */
    @GetMapping("/disk/usage")
    public ResponseEntity<Integer> getDiskUsage() {
        return ResponseEntity.ok(diskService.getOverallDiskLoad());
    }
}
