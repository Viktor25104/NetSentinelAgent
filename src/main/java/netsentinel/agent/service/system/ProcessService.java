package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.ProcessInfoDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
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
        Map<Integer, Integer> cpuByPid = getCpuLoadByPid();

        try {
            Process process = Runtime.getRuntime().exec("tasklist /FO CSV /NH");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName("windows-1251"))
            );

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.replaceAll("^\"|\"$", "").split("\",\"");
                if (fields.length < 5) continue;

                int pid = tryParseInt(fields[1]);
                long memRaw = parseMemory(fields[4]);
                Long mem = memRaw;

                Integer cpuRaw = cpuByPid.get(pid);
                Double cpu = cpuRaw != null ? cpuRaw.doubleValue() : null;

                ProcessInfoDto dto = new ProcessInfoDto(
                        pid,
                        fields[0],
                        fields[2],
                        tryParseInt(fields[3]),
                        mem,
                        null,
                        cpu
                );

                if (!isClearlySystemProcess(dto)) {
                    processes.add(dto);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, ProcessInfoDto> uniqueByName = new HashMap<>();
        for (ProcessInfoDto p : processes) {
            String key = p.name().toLowerCase();
            ProcessInfoDto existing = uniqueByName.get(key);

            if (existing == null ||
                    (Optional.ofNullable(p.cpu()).orElse(0.0) > Optional.ofNullable(existing.cpu()).orElse(0.0)) ||
                    (Optional.ofNullable(p.memoryUsage()).orElse(0L) > Optional.ofNullable(existing.memoryUsage()).orElse(0L))
            ) {
                uniqueByName.put(key, p); // оставим того, кто "жирнее"
            }
        }
        return new ArrayList<>(uniqueByName.values());

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

    private boolean isClearlySystemProcess(ProcessInfoDto p) {
        String name = p.name().toLowerCase();
        String session = p.sessionName() != null ? p.sessionName().toLowerCase() : "";

        return name.startsWith("system") ||
                name.contains("svchost") ||
                name.contains("csrss") ||
                name.contains("wininit") ||
                name.contains("winlogon") ||
                name.contains("services") ||
                name.contains("registry") ||
                name.contains("lsass") ||
                name.contains("fontdrvhost") ||
                name.contains("idle") ||
                session.equals("services") ||
                session.equals("system") ||
                session.equals("idle");
    }

    private boolean isUserProgram(ProcessInfoDto p) {
        String session = Optional.ofNullable(p.sessionName()).orElse("").toLowerCase();
        Long mem = Optional.ofNullable(p.memoryUsage()).orElse(0L);
        Double cpu = Optional.ofNullable(p.cpu()).orElse(0.0);

        return session.equals("console") && (cpu > 0 || mem > 10_000);
    }


    private Map<Integer, Integer> getCpuLoadByPid() {
        Map<Integer, Integer> cpuMap = new HashMap<>();

        try {
            Process proc = Runtime.getRuntime().exec("wmic path Win32_PerfFormattedData_PerfProc_Process get IDProcess,PercentProcessorTime /FORMAT:CSV");
            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                // Пропускаем заголовки и пустые строки
                if (line.isBlank() || line.contains("Node")) continue;

                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                try {
                    int pid = Integer.parseInt(parts[1].trim());
                    int cpu = Integer.parseInt(parts[2].trim());
                    cpuMap.put(pid, cpu);
                } catch (NumberFormatException ignored) {}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return cpuMap;
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

    private long parseMemory(String raw) {
        try {
            return Long.parseLong(raw.replaceAll("[^\\d]", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
