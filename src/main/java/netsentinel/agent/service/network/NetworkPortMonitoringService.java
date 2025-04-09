package netsentinel.agent.service.network;

import netsentinel.agent.dto.network.NetworkPortDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
            Process process = Runtime.getRuntime().exec("netstat -an");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("  TCP") || line.startsWith("  UDP")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 4) {
                        String protocol = parts[0];
                        String local = parts[1];
                        String state = parts.length >= 4 ? parts[3] : "UNKNOWN";

                        String port = local.substring(local.lastIndexOf(':') + 1);
                        ports.add(new NetworkPortDto(
                                tryParseInt(port),
                                protocol,
                                "-",
                                state
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

    private int tryParseInt(String val) {
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return -1;
        }
    }
}
