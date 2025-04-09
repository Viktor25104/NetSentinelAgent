package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.RamInfoDto;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import org.springframework.stereotype.Service;

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

    public RamInfoDto getRamInfo() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        return new RamInfoDto(total, used, available, getRamLoad());
    }
}
