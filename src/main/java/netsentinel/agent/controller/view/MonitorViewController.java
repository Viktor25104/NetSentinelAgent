package netsentinel.agent.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MonitorViewController {

    @GetMapping("/monitor/ports")
    public String showPortsMonitorPage() {
        return "dashboard/ports";
    }
}
