package netsentinel.agent.service.network;

import netsentinel.agent.dto.network.NetworkInterfaceDto;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetworkService {

    private final List<NetworkIF> interfaces;

    public NetworkService() {
        this.interfaces = new SystemInfo().getHardware().getNetworkIFs();
    }

    public List<NetworkInterfaceDto> getNetworkInfo() {
        List<NetworkInterfaceDto> result = new ArrayList<>();

        for (NetworkIF net : interfaces) {
            net.updateAttributes();
            result.add(new NetworkInterfaceDto(
                    net.getName(),
                    net.getDisplayName(),
                    net.getMacaddr(),
                    net.getIPv4addr(),
                    net.getIPv6addr(),
                    net.getBytesSent(),
                    net.getBytesRecv(),
                    net.getSpeed()
            ));
        }

        return result;
    }
}
