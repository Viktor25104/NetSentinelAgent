package netsentinel.agent.controller.view;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class SnifferViewController {

    private final SnifferService snifferService;

    @GetMapping("/sniffer")
    public String index(Model model) {
        model.addAttribute("interfaces", snifferService.getNetworkInterfaces());
        return "dashboard/sniffer";
    }
}
