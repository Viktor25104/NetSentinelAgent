package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.CpuInfoDto;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import org.springframework.stereotype.Service;

@Service
public class CpuService {

    private final CentralProcessor processor;
    private long[] prevTicks;

    public CpuService() {
        SystemInfo systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    public double getCpuLoad() {
        long[] newTicks = processor.getSystemCpuLoadTicks();
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100.0;
        prevTicks = newTicks;
        return Math.round(load * 10.0) / 10.0;
    }

    public CpuInfoDto getCpuInfo() {
        return new CpuInfoDto(
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount(),
                getCpuLoad()
        );
    }
}
