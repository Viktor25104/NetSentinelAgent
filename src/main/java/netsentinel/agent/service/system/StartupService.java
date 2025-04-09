package netsentinel.agent.service.system;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class StartupService {

    public List<Map<String, Object>> getStartupList() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? getWindowsStartup() : List.of(); // пока Linux не реализован
    }

    private List<Map<String, Object>> getWindowsStartup() {
        List<Map<String, Object>> services = new ArrayList<>();

        String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File folder = new File(startupFolder);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", file.getName());
                entry.put("location", "Startup Folder");
                entry.put("enabled", true);
                services.add(entry);
            }
        }

        // TODO: можно добавить и реестр, если нужно

        return services;
    }

    public boolean toggleService(String serviceName, boolean enable) {
        String os = System.getProperty("os.name").toLowerCase();
        if (!os.contains("win")) return false;

        String startupFolder = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup";
        File file = new File(startupFolder + "\\" + serviceName);

        try {
            if (enable) {
                // Для демонстрации просто создадим пустой .lnk файл
                if (!file.exists()) {
                    return file.createNewFile();
                }
            } else {
                return file.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
