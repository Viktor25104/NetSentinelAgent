package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.DiskInfoDto;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для сбора информации о дисках и их загрузке.
 * Использует OSHI для работы с файловыми системами.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class DiskService {

    private final FileSystem fileSystem;

    public DiskService() {
        SystemInfo systemInfo = new SystemInfo();
        this.fileSystem = systemInfo.getOperatingSystem().getFileSystem();
    }

    /**
     * Возвращает список всех логических дисков с информацией о загрузке.
     *
     * @return список {@link DiskInfoDto}
     */
    public List<DiskInfoDto> getDisksInfo() {
        List<DiskInfoDto> disks = new ArrayList<>();

        for (OSFileStore fs : fileSystem.getFileStores()) {
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            long used = total - usable;
            double usage = total > 0 ? ((double) used / total) * 100.0 : 0;

            disks.add(new DiskInfoDto(
                    fs.getName(),
                    fs.getMount(),
                    fs.getType(),
                    total,
                    used,
                    usable,
                    Math.round(usage * 10.0) / 10.0
            ));
        }

        return disks;
    }

    /**
     * Возвращает усреднённую загрузку всех дисков.
     *
     * @return число от 0 до 100
     */
    public int getOverallDiskLoad() {
        List<DiskInfoDto> disks = getDisksInfo();
        if (disks.isEmpty()) return 0;

        double sum = disks.stream()
                .mapToDouble(DiskInfoDto::usedPercent)
                .sum();

        return (int) Math.round(sum / disks.size());
    }
}
