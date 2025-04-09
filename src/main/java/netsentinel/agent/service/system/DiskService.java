package netsentinel.agent.service.system;

import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DiskService {

    private final FileSystem fileSystem;

    public DiskService() {
        SystemInfo systemInfo = new SystemInfo();
        this.fileSystem = systemInfo.getOperatingSystem().getFileSystem();
    }

    public List<Map<String, Object>> getDisksInfo() {
        List<Map<String, Object>> disks = new ArrayList<>();

        for (OSFileStore fs : fileSystem.getFileStores()) {
            long total = fs.getTotalSpace();
            long usable = fs.getUsableSpace();
            long used = total - usable;
            double usage = total > 0 ? ((double) used / total) * 100.0 : 0;

            disks.add(Map.of(
                    "name", fs.getName(),
                    "mount", fs.getMount(),
                    "type", fs.getType(),
                    "totalBytes", total,
                    "usedBytes", used,
                    "freeBytes", usable,
                    "usedPercent", Math.round(usage * 10.0) / 10.0
            ));
        }

        return disks;
    }

    public int getOverallDiskLoad() {
        List<Map<String, Object>> disks = getDisksInfo();

        if (disks.isEmpty()) return 0;

        double sum = 0;
        for (Map<String, Object> disk : disks) {
            sum += ((Number) disk.get("usedPercent")).doubleValue();
        }

        return (int) Math.round(sum / disks.size());
    }
}
