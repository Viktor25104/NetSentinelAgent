package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.system.StartupService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/startup")
public class StartupApiController {

    private final StartupService startupService;

    @GetMapping("/toggle")
    public Map<String, Object> toggleStartupService(
            @RequestParam String serviceName,
            @RequestParam boolean enable
    ) {
        boolean success = startupService.toggleService(serviceName, enable);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success
                ? "Служба " + serviceName + " успешно " + (enable ? "запущена" : "остановлена")
                : "Не удалось изменить состояние службы " + serviceName);
        return response;
    }
}
