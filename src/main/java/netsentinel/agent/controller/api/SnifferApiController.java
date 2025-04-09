package netsentinel.agent.controller.api;

import lombok.RequiredArgsConstructor;
import netsentinel.agent.dto.network.PacketDto;
import netsentinel.agent.service.network.SnifferService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sniffer")
public class SnifferApiController {

    private final SnifferService snifferService;

    @PostMapping("/capture/start")
    public String startCapture(@RequestParam(required = false) String interfaceName) {
        snifferService.startCapture(interfaceName);
        return "Capturing started";
    }

    @PostMapping("/capture/stop")
    public String stopCapture() {
        snifferService.stopCapture();
        return "Capturing stopped";
    }

    @PostMapping("/capture/clear")
    public String clearCapture() {
        snifferService.clearCapture();
        return "Capture cleared";
    }

    @GetMapping("/packets")
    public List<PacketDto> getPackets() {
        return snifferService.getCapturedPackets();
    }
}
