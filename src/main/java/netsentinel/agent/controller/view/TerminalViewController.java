package netsentinel.agent.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отображения страницы терминала.
 * Используется для удалённого запуска команд.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Controller
public class TerminalViewController {

    /**
     * Открывает страницу терминала.
     *
     * @return шаблон terminal/index.html
     */
    @GetMapping("/terminal")
    public String terminalPage() {
        return "terminal/index";
    }
}
