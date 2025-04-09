package netsentinel.agent.controller.api;

import netsentinel.agent.config.MonitoringConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/config")
public class ConfigApiController {

    private final MonitoringConfig config;

    @Qualifier("legacyContextRefresher")
    private final ContextRefresher contextRefresher;

    public ConfigApiController(
            MonitoringConfig config,
            @Qualifier("legacyContextRefresher") ContextRefresher contextRefresher
    ) {
        this.config = config;
        this.contextRefresher = contextRefresher;
    }

    @GetMapping("/current")
    public ResponseEntity<MonitoringConfig> getCurrentConfig() {
        return ResponseEntity.ok(config);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshConfig() {
        Set<String> keys = contextRefresher.refresh();
        return ResponseEntity.ok("Configuration refreshed. Updated keys: " + keys);
    }
}
