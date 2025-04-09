package netsentinel.agent.dto.network;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для описания одного захваченного сетевого пакета.
 * Используется в сниффере и отображается в UI как строка захвата.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketDto {

    /** Временная метка захвата пакета */
    private String timestamp;

    /** IP-адрес источника */
    private String sourceIP;

    /** IP-адрес назначения */
    private String destinationIP;

    /** Порт источника */
    private int sourcePort;

    /** Порт назначения */
    private int destinationPort;

    /** Протокол (TCP, UDP, ICMP и т.д.) */
    private String protocol;

    /** Размер пакета (в байтах) */
    private int length;

    /** Содержимое (Payload или краткое описание) */
    private String info;
}
