package netsentinel.agent.service.system;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class RamService {

    private final GlobalMemory memory;

    public RamService() {
        SystemInfo systemInfo = new SystemInfo();
        this.memory = systemInfo.getHardware().getMemory();
    }

    public double getRamLoad() {
        long total = memory.getTotal();
        long used = total - memory.getAvailable();
        return Math.round(((double) used / total) * 1000.0) / 10.0;
    }

    public Map<String, Object> getRamInfo() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        return Map.of(
                "totalBytes", total,
                "usedBytes", used,
                "freeBytes", available,
                "usedPercent", getRamLoad()
        );
    }
}
