package netsentinel.agent.controller.api;

import netsentinel.agent.config.MonitoringConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * REST API-контроллер для работы с конфигурацией мониторинга.
 * Позволяет получить текущую конфигурацию агента и инициировать её обновление во время выполнения.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@RestController
@RequestMapping("/api/config")
public class ConfigApiController {

    private final MonitoringConfig config;
    private final ContextRefresher contextRefresher;

    public ConfigApiController(
            MonitoringConfig config,
            @Qualifier("legacyContextRefresher") ContextRefresher contextRefresher
    ) {
        this.config = config;
        this.contextRefresher = contextRefresher;
    }

    /**
     * Возвращает текущую конфигурацию мониторинга.
     *
     * @return {@link MonitoringConfig} сериализованный в JSON
     */
    @GetMapping("/current")
    public ResponseEntity<MonitoringConfig> getCurrentConfig() {
        return ResponseEntity.ok(config);
    }

    /**
     * Перечитывает и обновляет конфигурацию из application.yml / централизованного конфига.
     *
     * @return сообщение со списком обновлённых ключей
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshConfig() {
        Set<String> keys = contextRefresher.refresh();
        return ResponseEntity.ok("Конфигурация обновлена. Изменено: " + keys);
    }
}
