package netsentinel.agent.controller.api;

import netsentinel.agent.service.system.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API-контроллер для управления процессами на сервере.
 * Позволяет завершать запущенные процессы по PID.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/process")
public class ProcessApiController {

    private final ProcessService processService;

    public ProcessApiController(ProcessService processService) {
        this.processService = processService;
    }

    /**
     * Завершает процесс по указанному PID.
     *
     * @param pid идентификатор процесса
     * @return true, если процесс был успешно завершён
     */
    @GetMapping("/kill/{pid}")
    public ResponseEntity<Boolean> kill(@PathVariable int pid) {
        System.out.println("Kill process with PID: " + pid);
        return ResponseEntity.ok(processService.killProcess(pid));
    }
}
