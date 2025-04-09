package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.system.ProcessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/process")
public class ProcessApiController {

    private final ProcessService processService;

    @GetMapping("/kill")
    public Map<String, Object> killProcess(@RequestParam int pid) {
        boolean success = processService.killProcess(pid);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success
                ? "Процесс с PID " + pid + " успешно завершен"
                : "Не удалось завершить процесс с PID " + pid);
        return response;
    }

}
