package netsentinel.agent.controller.view;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для страницы сниффера.
 * Передаёт список доступных сетевых интерфейсов в шаблон.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Controller
@RequiredArgsConstructor
public class SnifferViewController {

    private final SnifferService snifferService;

    /**
     * Показывает страницу сниффера с выбором интерфейса.
     *
     * @param model модель Thymeleaf
     * @return шаблон dashboard/sniffer.html
     */
    @GetMapping("/sniffer")
    public String index(Model model) {
        model.addAttribute("interfaces", snifferService.getNetworkInterfaces());
        return "dashboard/sniffer";
    }
}
