package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.StartupItemDto;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Сервис для работы со службами автозагрузки (startup items).
 * Поддерживает только Windows.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class StartupService {

    /**
     * Возвращает список элементов автозагрузки.
     *
     * @return список {@link StartupItemDto}
     */
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

    /**
     * Включает или отключает элемент автозагрузки.
     *
     * @param serviceName имя файла службы
     * @param enable true — включить, false — отключить
     * @return true, если операция успешна
     */
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

