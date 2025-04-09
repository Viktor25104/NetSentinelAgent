package netsentinel.agent.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отображения страницы мониторинга портов.
 * Показывает информацию в представлении dashboard/ports.html.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Controller
public class MonitorViewController {

    /**
     * Показывает страницу мониторинга портов.
     *
     * @return путь к шаблону dashboard/ports.html
     */
    @GetMapping("/monitor/ports")
    public String showPortsMonitorPage() {
        return "dashboard/ports";
    }
}
