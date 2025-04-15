package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.CpuInfoDto;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import org.springframework.stereotype.Service;

/**
 * Сервис получения информации о процессоре.
 * Использует библиотеку OSHI для сбора данных о CPU.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class CpuService {

    private final CentralProcessor processor;
    private long[] prevTicks;

    public CpuService() {
        SystemInfo systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.prevTicks = processor.getSystemCpuLoadTicks();
    }

    /**
     * Возвращает текущую загрузку CPU в процентах.
     *
     * @return загрузка (0.0–100.0)
     */
    public double getCpuLoad() {
        long[] newTicks = processor.getSystemCpuLoadTicks();
        double load = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 1000.0;
        prevTicks = newTicks;
        return Math.round(load * 10.0) / 10.0;
    }

    /**
     * Возвращает полную информацию о процессоре.
     *
     * @return DTO {@link CpuInfoDto}
     */
    public CpuInfoDto getCpuInfo() {
        return new CpuInfoDto(
                processor.getProcessorIdentifier().getName(),
                processor.getPhysicalProcessorCount(),
                processor.getLogicalProcessorCount(),
                getCpuLoad()
        );
    }
}
