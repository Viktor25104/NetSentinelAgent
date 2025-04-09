package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.ProcessInfoDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Сервис управления и мониторинга запущенных процессов.
 * Поддерживает Windows и Linux.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class ProcessService {

    /**
     * Возвращает список запущенных процессов.
     * Формат зависит от ОС.
     *
     * @return список {@link ProcessInfoDto}
     */
    public List<ProcessInfoDto> getProcessList() {
        return isWindows() ? getWindowsProcesses() : getLinuxProcesses();
    }

    private List<ProcessInfoDto> getWindowsProcesses() {
        List<ProcessInfoDto> processes = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FO CSV /NH");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.replaceAll("^\"|\"$", "").split("\",\"");
                if (fields.length < 5) continue;

                processes.add(new ProcessInfoDto(
                        tryParseInt(fields[1]),
                        fields[0],
                        fields[2],
                        tryParseInt(fields[3]),
                        fields[4],
                        null,
                        null
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processes;
    }

    private List<ProcessInfoDto> getLinuxProcesses() {
        List<ProcessInfoDto> processes = new ArrayList<>();
        try {
            String[] cmd = {"/bin/sh", "-c", "ps -eo pid,comm,%mem,%cpu --sort=-%mem"};
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            reader.readLine(); // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length < 4) continue;

                processes.add(new ProcessInfoDto(
                        tryParseInt(parts[0]),
                        parts[1],
                        null,
                        null,
                        null,
                        tryParseDouble(parts[2]),
                        tryParseDouble(parts[3])
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processes;
    }

    /**
     * Убивает процесс по его PID.
     *
     * @param pid идентификатор процесса
     * @return true, если успешно
     */
    public boolean killProcess(int pid) {
        try {
            Process proc = isWindows()
                    ? Runtime.getRuntime().exec("taskkill /PID " + pid + " /F")
                    : Runtime.getRuntime().exec("kill -9 " + pid);
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    private int tryParseInt(String val) {
        try {
            return Integer.parseInt(val.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return -1;
        }
    }

    private double tryParseDouble(String val) {
        try {
            return Double.parseDouble(val.replace(",", "."));
        } catch (Exception e) {
            return 0.0;
        }
    }
}
