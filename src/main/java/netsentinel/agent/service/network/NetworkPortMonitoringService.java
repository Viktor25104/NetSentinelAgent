package netsentinel.agent.service.network;

import netsentinel.agent.dto.network.NetworkPortDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Сервис для мониторинга открытых портов и активных соединений.
 * <p>
 * Использует системные команды:
 * <ul>
 *     <li><b>Windows</b>: {@code netstat -an}</li>
 *     <li><b>Linux/macOS</b>: {@code ss -tuln}</li>
 * </ul>
 * Возвращает список DTO {@link NetworkPortDto}.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class NetworkPortMonitoringService {

    /**
     * Возвращает список открытых портов в зависимости от ОС.
     *
     * @return список {@link NetworkPortDto}
     */
    public List<NetworkPortDto> getPorts() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? getWindowsPorts() : getUnixPorts();
    }

    // Методы ниже приватны, так как специфичны под платформу.

    private List<NetworkPortDto> getWindowsPorts() {
        List<NetworkPortDto> ports = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec("netstat -ano");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Для сопоставления PID -> имя процесса
            Map<String, String> pidToName = getProcessNamesMap();

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  TCP") || line.startsWith("  UDP")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        String protocol = parts[0];
                        String local = parts[1];
                        String state = parts[3];
                        String pid = parts[4];

                        int port = tryParseInt(local.substring(local.lastIndexOf(':') + 1));
                        if (port == 0) continue;

                        // Игнорировать шум, например, 135/445 и TIME_WAIT и др.
                        if (isSystemPort(port) || "TIME_WAIT".equals(state) || "CLOSE_WAIT".equals(state)) continue;

                        String readableService = pidToName.getOrDefault(pid, "Неизвестно");

                        ports.add(new NetworkPortDto(
                                port,
                                protocol,
                                readableService,
                                simplifyState(state)
                        ));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ports;
    }


    private List<NetworkPortDto> getUnixPorts() {
        List<NetworkPortDto> ports = new ArrayList<>();
        try {
            String[] cmd = {"/bin/sh", "-c", "ss -tuln"};
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine(); // пропускаем заголовок

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 5) {
                    String proto = parts[0];
                    String local = parts[4];
                    String port = local.substring(local.lastIndexOf(':') + 1);
                    ports.add(new NetworkPortDto(tryParseInt(port), proto, "-", "LISTEN"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ports;
    }

    private boolean isSystemPort(int port) {
        return port < 1024 || List.of(135, 445, 139).contains(port);
    }

    private String simplifyState(String state) {
        return switch (state) {
            case "LISTENING" -> "Ожидание";
            case "ESTABLISHED" -> "Установлено";
            case "TIME_WAIT" -> "Ожидание завершения";
            case "CLOSE_WAIT" -> "Закрытие";
            case "SYN_SENT" -> "Установка соединения";
            default -> "Неизвестно";
        };
    }

    private Map<String, String> getProcessNamesMap() {
        Map<String, String> map = new HashMap<>();
        try {
            Process tasklist = Runtime.getRuntime().exec("tasklist /FO CSV /NH");
            BufferedReader reader = new BufferedReader(new InputStreamReader(tasklist.getInputStream(), Charset.forName("windows-1251")));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.replaceAll("^\"|\"$", "").split("\",\"");
                if (parts.length >= 2) {
                    map.put(parts[1], parts[0]); // PID -> Process name
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private int tryParseInt(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
