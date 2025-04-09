package netsentinel.agent.service.network;

import netsentinel.agent.dto.network.NetworkInterfaceDto;
import oshi.SystemInfo;
import oshi.hardware.NetworkIF;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для получения информации о сетевых интерфейсах.
 * <p>
 * Использует библиотеку OSHI для сбора подробной информации:
 * MAC, IP, скорость интерфейса, трафик и др.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Service
public class NetworkService {

    private final List<NetworkIF> interfaces;

    /**
     * Инициализирует список сетевых интерфейсов при создании бина.
     */
    public NetworkService() {
        this.interfaces = new SystemInfo().getHardware().getNetworkIFs();
    }

    /**
     * Возвращает актуальные данные по каждому сетевому интерфейсу.
     * Обновляет информацию на каждый вызов.
     *
     * @return список {@link NetworkInterfaceDto}
     */
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
