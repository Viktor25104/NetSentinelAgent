package netsentinel.agent.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TerminalViewController {

    @GetMapping("/terminal")
    public String terminalPage() {
        return "terminal/index";
    }
}
