package netsentinel.agent.service.system;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class ProcessService {

    public List<Map<String, Object>> getProcessList() {
        String os = System.getProperty("os.name").toLowerCase();
        return os.contains("win") ? getWindowsProcesses() : getLinuxProcesses();
    }

    private List<Map<String, Object>> getWindowsProcesses() {
        List<Map<String, Object>> processes = new ArrayList<>();

        try {
            Process process = Runtime.getRuntime().exec("tasklist /FO CSV /NH");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.replaceAll("^\"|\"$", "").split("\",\"");
                if (fields.length < 5) continue;

                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", fields[0]);
                entry.put("pid", tryParseInt(fields[1]));
                entry.put("sessionName", fields[2]);
                entry.put("sessionId", tryParseInt(fields[3]));
                entry.put("memoryUsage", fields[4]);

                processes.add(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return processes;
    }

    private List<Map<String, Object>> getLinuxProcesses() {
        List<Map<String, Object>> processes = new ArrayList<>();

        try {
            String[] cmd = {"/bin/sh", "-c", "ps -eo pid,comm,%mem,%cpu --sort=-%mem"};
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            reader.readLine(); // skip header

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+", 4);
                if (parts.length < 4) continue;

                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("pid", tryParseInt(parts[0]));
                entry.put("name", parts[1]);
                entry.put("mem", tryParseDouble(parts[2]));
                entry.put("cpu", tryParseDouble(parts[3]));

                processes.add(entry);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return processes;
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

    public boolean killProcess(int pid) {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            Process proc;
            if (os.contains("win")) {
                proc = Runtime.getRuntime().exec("taskkill /PID " + pid + " /F");
            } else {
                proc = Runtime.getRuntime().exec("kill -9 " + pid);
            }
            return proc.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
