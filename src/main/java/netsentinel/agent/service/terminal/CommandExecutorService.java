package netsentinel.agent.service.terminal;

import netsentinel.agent.model.CommandResponse;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class CommandExecutorService {

    public CommandResponse executeCommand(String command) {
        String os = System.getProperty("os.name").toLowerCase();
        String[] cmd;

        if (os.contains("win")) {
            cmd = new String[]{"cmd.exe", "/c", command};
        } else {
            cmd = new String[]{"/bin/sh", "-c", command};
        }

        String output = "";
        String error = "";
        boolean success = false;

        try {
            Process process = new ProcessBuilder(cmd).start();

            output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));

            error = new BufferedReader(new InputStreamReader(process.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));

            int exitCode = process.waitFor();
            success = (exitCode == 0);

        } catch (Exception e) {
            error = e.getMessage();
        }

        return new CommandResponse(output, error, success);
    }
}
