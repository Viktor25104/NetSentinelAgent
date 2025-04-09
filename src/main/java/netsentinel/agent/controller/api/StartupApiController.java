package netsentinel.agent.controller.api;

import netsentinel.agent.service.system.StartupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API-контроллер для включения или отключения служб автозапуска.
 * Предназначен для работы с файлами в папке автозагрузки Windows.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/startup")
public class StartupApiController {

    private final StartupService service;

    public StartupApiController(StartupService service) {
        this.service = service;
    }

    /**
     * Включает или отключает службу автозагрузки по имени.
     *
     * @param name имя файла/службы
     * @param enable true для включения, false — для отключения
     * @return true, если операция выполнена успешно
     */
    @PostMapping("/toggle")
    public ResponseEntity<Boolean> toggle(
            @RequestParam String name,
            @RequestParam boolean enable
    ) {
        return ResponseEntity.ok(service.toggleService(name, enable));
    }
}
