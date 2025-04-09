package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.RamInfoDto;
import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import org.springframework.stereotype.Service;

/**
 * Сервис получения информации об оперативной памяти (RAM).
 * Использует библиотеку OSHI для получения информации об объёме и загрузке.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class RamService {

    private final GlobalMemory memory;

    public RamService() {
        SystemInfo systemInfo = new SystemInfo();
        this.memory = systemInfo.getHardware().getMemory();
    }

    /**
     * Возвращает загрузку RAM в процентах.
     *
     * @return double от 0.0 до 100.0
     */
    public double getRamLoad() {
        long total = memory.getTotal();
        long used = total - memory.getAvailable();
        return Math.round(((double) used / total) * 1000.0) / 10.0;
    }

    /**
     * Возвращает полную информацию о состоянии RAM.
     *
     * @return {@link RamInfoDto}
     */
    public RamInfoDto getRamInfo() {
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        return new RamInfoDto(total, used, available, getRamLoad());
    }
}
