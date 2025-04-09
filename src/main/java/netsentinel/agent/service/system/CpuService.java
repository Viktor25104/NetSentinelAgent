package netsentinel.agent.service.system;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CpuService {

    private final CentralProcessor processor;
    private long[] prevTicks;

    public CpuService() {
        SystemInfo systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks(); // начальное состояние
    }

    public double getCpuLoad() {
        long[] newTicks = processor.getSystemCpuLoadTicks();
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
        prevTicks = newTicks;
        return Math.round(load * 10.0) / 10.0;
    }

    public Map<String, Object> getCpuInfo() {
        return Map.of(
                "name", processor.getProcessorIdentifier().getName(),
                "physicalCores", processor.getPhysicalProcessorCount(),
                "logicalCores", processor.getLogicalProcessorCount(),
                "load", getCpuLoad()
        );
    }
}
