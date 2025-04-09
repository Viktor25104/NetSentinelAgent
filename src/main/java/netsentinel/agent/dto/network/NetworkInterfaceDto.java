package netsentinel.agent.dto.network;

/**
 * DTO, описывающий сетевой интерфейс устройства.
 * <p>
 * Используется для отображения информации о сетевых адаптерах,
 * включая MAC, IP-адреса, скорость и статистику передачи данных.
 *
 * @param name системное имя интерфейса (например, eth0)
 * @param displayName отображаемое имя (например, Ethernet 2)
 * @param mac MAC-адрес
 * @param ipv4 список IPv4-адресов
 * @param ipv6 список IPv6-адресов
 * @param bytesSent количество отправленных байт
 * @param bytesRecv количество полученных байт
 * @param speed скорость интерфейса (в бит/сек)
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
public record NetworkInterfaceDto(
        String name,
        String displayName,
        String mac,
        String[] ipv4,
        String[] ipv6,
        long bytesSent,
        long bytesRecv,
        long speed
) {}
