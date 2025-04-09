package netsentinel.agent.service.network;

import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class NetworkService {

    private final List<NetworkIF> interfaces;

    public NetworkService() {
        this.interfaces = new SystemInfo().getHardware().getNetworkIFs();
    }

    public List<Map<String, Object>> getNetworkInfo() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (NetworkIF net : interfaces) {
            net.updateAttributes();

            Map<String, Object> info = new LinkedHashMap<>();
            info.put("name", net.getName());
            info.put("displayName", net.getDisplayName());
            info.put("mac", net.getMacaddr());
            info.put("ipv4", Arrays.toString(net.getIPv4addr()));
            info.put("ipv6", Arrays.toString(net.getIPv6addr()));
            info.put("bytesSent", net.getBytesSent());
            info.put("bytesRecv", net.getBytesRecv());
            info.put("speed", net.getSpeed());

            result.add(info);
        }

        return result;
    }
}
