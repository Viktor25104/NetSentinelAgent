package netsentinel.agent.service.network;

import netsentinel.agent.model.NetworkPort;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class NetworkPortMonitoringService {

    public List<NetworkPort> getPorts() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? getWindowsPorts() : getUnixPorts();
    }

    private List<NetworkPort> getWindowsPorts() {
        List<NetworkPort> ports = new ArrayList<>();
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
                        ports.add(new NetworkPort(
                                tryParseInt(port),
                                protocol,
                                "-", // сервисов нет в netstat
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

    private List<NetworkPort> getUnixPorts() {
        List<NetworkPort> ports = new ArrayList<>();
        try {
            String[] cmd = {"/bin/sh", "-c", "ss -tuln"};
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            reader.readLine(); // skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 5) {
                    String proto = parts[0];
                    String local = parts[4];
                    String port = local.substring(local.lastIndexOf(':') + 1);
                    ports.add(new NetworkPort(tryParseInt(port), proto, "-", "LISTEN"));
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
