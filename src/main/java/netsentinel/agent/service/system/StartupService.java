package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.StartupItemDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class StartupService {

    public List<StartupItemDto> getStartupList() {
        return isWindows() ? getWindowsStartup() : List.of();
    }

    private List<StartupItemDto> getWindowsStartup() {
        List<StartupItemDto> services = new ArrayList<>();

        String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File folder = new File(startupFolder);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                services.add(new StartupItemDto(file.getName(), "Startup Folder", true));
            }
        }

        return services;
    }

    public boolean toggleService(String serviceName, boolean enable) {
        if (!isWindows()) return false;

        String path = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + serviceName;
        File file = new File(path);

        try {
            if (enable) {
                return !file.exists() && file.createNewFile();
            } else {
                return file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}

