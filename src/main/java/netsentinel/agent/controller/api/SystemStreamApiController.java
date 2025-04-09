package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.config.MonitoringConfig;
import netsentinel.agent.service.network.NetworkService;
import netsentinel.agent.service.system.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
public class SystemStreamApiController {

    private final CpuService cpuService;
    private final RamService ramService;
    private final DiskService diskService;
    private final NetworkService networkService;
    private final ProcessService processService;
    private final StartupService startupService;
    private final MonitoringConfig config;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @GetMapping("/api/system/updates")
    public SseEmitter streamUpdates() {
        SseEmitter emitter = new SseEmitter(-1L);

        executorService.execute(() -> {
            try {
                while (true) {
                    Map<String, Object> systemData = new HashMap<>();
                    systemData.put("cpu", cpuService.getCpuInfo());
                    systemData.put("ram", ramService.getRamInfo());
                    systemData.put("disks", diskService.getDisksInfo());
                    systemData.put("network", networkService.getNetworkInfo());
                    systemData.put("processes", processService.getProcessList());
                    systemData.put("startupServices", startupService.getStartupList());

                    emitter.send(SseEmitter.event()
                            .name("system-update")
                            .data(systemData));

                    long pollingRate = getMinimalPollingRate();
                    Thread.sleep(pollingRate);
                }
            } catch (IOException | InterruptedException e) {
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(emitter::complete);

        return emitter;
    }

    private long getMinimalPollingRate() {
        return Math.min(
                Math.min(
                        Math.min(
                                Math.min(
                                        Math.min(config.getPolling().getCpu(), config.getPolling().getRam()),
                                        config.getPolling().getDisk()
                                ),
                                config.getPolling().getNetwork()
                        ),
                        config.getPolling().getProcesses()
                ),
                config.getPolling().getStartup()
        );
    }

}
