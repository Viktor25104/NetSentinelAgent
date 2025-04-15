package netsentinel.agent.service.system;

import netsentinel.agent.dto.system.StartupItemDto;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

@Service
public class StartupService {

    public List<StartupItemDto> getStartupList() {
        return isWindows() ? getWindowsStartup() : List.of();
    }

    private List<StartupItemDto> getWindowsStartup() {
        List<StartupItemDto> items = new ArrayList<>();

        items.addAll(parseStartupFolder(System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup"));
        items.addAll(parseStartupFolder(System.getenv("PROGRAMDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup"));
        items.addAll(getStartupFromRegistryViaPowerShell());

        return items;
    }

    private List<StartupItemDto> parseStartupFolder(String path) {
        List<StartupItemDto> list = new ArrayList<>();
        File folder = new File(path);

        if (folder.exists() && folder.isDirectory()) {
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                list.add(new StartupItemDto(file.getName(), "Startup Folder", true));
            }
        }

        return list;
    }

    private List<StartupItemDto> getStartupFromRegistryViaPowerShell() {
        List<StartupItemDto> items = new ArrayList<>();

        items.addAll(readRegistryRunWithPowershell("HKCU"));
        items.addAll(readRegistryRunWithPowershell("HKLM"));

        System.out.println("✅ Startup from registry (PowerShell): " + items.size() + " items");
        return items;
    }

    private List<StartupItemDto> readRegistryRunWithPowershell(String hive) {
        List<StartupItemDto> items = new ArrayList<>();
        String regPath = hive + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Run";

        String command = String.format(
                "$items = Get-ItemProperty -Path 'Registry::%s' -ErrorAction SilentlyContinue; " +
                        "$items.PSObject.Properties | ForEach-Object { " +
                        "  [PSCustomObject]@{ Name = $_.Name; Value = $_.Value } } | ConvertTo-Json -Compress",
                regPath
        );

        try {
            ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-Command", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("windows-1251")));
            StringBuilder output = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            String json = output.toString().trim();

            if (!json.isEmpty() && json.startsWith("[")) {
                JSONArray array = new JSONArray(json);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    String name = obj.optString("Name", "");
                    if (!name.isEmpty()) {
                        Boolean enabled = checkStartupStatusFromApproved(hive, name);
                        if (enabled != null) {
                            items.add(new StartupItemDto(name, "Registry", enabled));
                            System.out.println("🔍 [" + hive + "] " + name + " -> StartupApproved: " + (enabled ? "Enabled" : "Disabled"));
                        }
                    }
                }
            } else if (!json.isEmpty()) {
                JSONObject obj = new JSONObject(json);
                String name = obj.optString("Name", "");
                if (!name.isEmpty()) {
                    Boolean enabled = checkStartupStatusFromApproved(hive, name);
                    if (enabled != null) {
                        items.add(new StartupItemDto(name, "Registry", enabled));
                        System.out.println("🔍 [" + hive + "] " + name + " -> StartupApproved: " + (enabled ? "Enabled" : "Disabled"));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка PowerShell при чтении автозагрузки из " + hive);
            e.printStackTrace();
        }

        return items;
    }

    private Boolean checkStartupStatusFromApproved(String hive, String name) {
        String approvedPath = hive + "\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\StartupApproved\\Run";

        String command = String.format(
                "$val = Get-ItemProperty -Path 'Registry::%s' -Name '%s' -ErrorAction SilentlyContinue; " +
                        "if ($val -ne $null) { $raw = $val.'%s'; if ($raw -is [byte[]]) { $raw[0] } else { -1 } } else { -1 }",
                approvedPath, name, name
        );

        try {
            ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-Command", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.forName("windows-1251")));
            String result = reader.readLine();

            if (result != null) {
                result = result.trim();
                switch (result) {
                    case "2": return true; // Отключён
                    case "3": return false;  // Включён
                    case "-1": return null; // Не найден — исключаем
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Ошибка проверки StartupApproved для: " + name);
            e.printStackTrace();
        }

        return null; // Неизвестно — не добавляем
    }



    private String extractJsonField(String json, String key) {
        String pattern = String.format("\"%s\":\"(.*?)\"", key);
        return json.matches(".*" + pattern + ".*") ? json.replaceAll(".*" + pattern + ".*", "$1") : "";
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

    public boolean toggleAutorun(String serviceName) {
        if (!isWindows()) return false;

        String path = System.getenv("APPDATA") + "\\Microsoft\\Windows\\Start Menu\\Programs\\Startup\\" + serviceName;
        File file = new File(path);

        boolean currentlyEnabled = file.exists();
        return toggleService(serviceName, !currentlyEnabled);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
