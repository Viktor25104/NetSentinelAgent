package netsentinel.agent.controller.api;

import netsentinel.agent.service.system.*;
import netsentinel.agent.service.network.NetworkPortMonitoringService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Контроллер, реализующий трансляцию системных метрик в реальном времени через SSE (Server-Sent Events).
 * Используется для динамических обновлений UI (например, графиков и таблиц).
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/stream")
public class SystemStreamApiController {

    private final CpuService cpuService;
    private final RamService ramService;
    private final DiskService diskService;
    private final ProcessService processService;
    private final StartupService startupService;
    private final NetworkPortMonitoringService portMonitoringService;

    public SystemStreamApiController(
            CpuService cpuService,
            RamService ramService,
            DiskService diskService,
            ProcessService processService,
            StartupService startupService,
            NetworkPortMonitoringService portMonitoringService) {
        this.cpuService = cpuService;
        this.ramService = ramService;
        this.diskService = diskService;
        this.processService = processService;
        this.startupService = startupService;
        this.portMonitoringService = portMonitoringService;
    }

    /**
     * Возвращает SSE-источник, передающий метрики каждые 3 секунды.
     *
     * @return {@link SseEmitter} с данными JSON
     */
    @GetMapping(path = "/system", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSystemInfo() {
        SseEmitter emitter = new SseEmitter();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("system")
                        .data(
                                new SystemMetricsSnapshot(
                                        cpuService.getCpuInfo(),
                                        ramService.getRamInfo(),
                                        diskService.getDisksInfo(),
                                        processService.getProcessList(),
                                        startupService.getStartupList(),
                                        portMonitoringService.getPorts()
                                )
                        ));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, 0, 3, TimeUnit.SECONDS);

        return emitter;
    }

    /**
     * Внутренний DTO для отправки нескольких метрик в одной точке SSE.
     *
     * @param cpu      CPU инфо
     * @param ram      RAM инфо
     * @param disks    диски
     * @param process  процессы
     * @param startup  автозапуск
     * @param ports    порты
     */
    public record SystemMetricsSnapshot(
            Object cpu,
            Object ram,
            Object disks,
            Object process,
            Object startup,
            Object ports
    ) {}
}
