package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.DiskInfoDto;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DiskService {

    private final FileSystem fileSystem;

    public DiskService() {
        SystemInfo systemInfo = new SystemInfo();
        this.fileSystem = systemInfo.getOperatingSystem().getFileSystem();
    }

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

    public int getOverallDiskLoad() {
        List<DiskInfoDto> disks = getDisksInfo();
        if (disks.isEmpty()) return 0;

        double sum = disks.stream()
                .mapToDouble(DiskInfoDto::usedPercent)
                .sum();

        return (int) Math.round(sum / disks.size());
    }
}
