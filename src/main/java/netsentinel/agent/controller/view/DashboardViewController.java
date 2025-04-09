package netsentinel.agent.controller.view;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.service.network.NetworkService;
import netsentinel.agent.service.system.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для отображения основной панели мониторинга.
 * Передаёт на страницу информацию о CPU, RAM, дисках, сети, процессах и автозагрузке.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Controller
@RequiredArgsConstructor
public class DashboardViewController {

    private final CpuService cpuService;
    private final RamService ramService;
    private final DiskService diskService;
    private final NetworkService networkService;
    private final ProcessService processService;
    private final StartupService startupService;

    /**
     * Загружает и передаёт данные на страницу панели мониторинга.
     *
     * @param model объект модели для Thymeleaf
     * @return шаблон dashboard/index.html
     */
    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        model.addAttribute("cpuInfo", cpuService.getCpuInfo());
        model.addAttribute("ramInfo", ramService.getRamInfo());
        model.addAttribute("disksInfo", diskService.getDisksInfo());
        model.addAttribute("networkInfo", networkService.getNetworkInfo());
        model.addAttribute("processList", processService.getProcessList());
        model.addAttribute("startupList", startupService.getStartupList());
        return "dashboard/index";
    }
}
