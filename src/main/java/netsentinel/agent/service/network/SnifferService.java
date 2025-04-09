package netsentinel.agent.service.network;

import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import netsentinel.agent.dto.network.PacketDto;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.util.*;

/**
 * Сервис захвата сетевых пакетов с использованием библиотеки Pcap4J.
 * <p>
 * Позволяет:
 * <ul>
 *     <li>Получить список интерфейсов</li>
 *     <li>Запустить и остановить захват трафика</li>
 *     <li>Обрабатывать пакеты и извлекать полезную нагрузку</li>
 *     <li>Передавать их в WebSocket клиенту</li>
 * </ul>
 * Работает с TCP, UDP, ICMP, Ethernet.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Slf4j
@Service
public class SnifferService {

    /**
     * -- GETTER --
     *  Возвращает список всех захваченных пакетов.
     *
     * @return список {@link PacketDto}
     */
    @Getter
    private final List<PacketDto> capturedPackets = Collections.synchronizedList(new ArrayList<>());
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${sysmonitor.network.maxPacketsCapture:100}")
    private int maxPacketsCapture;

    @Value("${sysmonitor.network.packetBufferSize:65536}")
    private int bufferSize;

    @Value("${sysmonitor.network.capturePayload:false}")
    private boolean capturePayload;

    private PcapHandle handle;

    public SnifferService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Получает список доступных сетевых интерфейсов.
     *
     * @return список {@link PcapNetworkInterface}
     */
    public List<PcapNetworkInterface> getNetworkInterfaces() {
        try {
            return Pcaps.findAllDevs();
        } catch (Exception e) {
            log.error("Ошибка при получении интерфейсов", e);
            return Collections.emptyList();
        }
    }

    /**
     * Очищает список сохранённых пакетов.
     */
    public void clearCapturedPackets() {
        capturedPackets.clear();
    }

    /**
     * Инициализирует и запускает сниффер на указанном интерфейсе.
     *
     * @param interfaceName имя интерфейса, если null — используется первый доступный
     */
    public void startCapture(String interfaceName) {
        try {
            PcapNetworkInterface nif = (interfaceName != null)
                    ? getNetworkInterfaces().stream().filter(n -> n.getName().equals(interfaceName)).findFirst().orElse(null)
                    : getNetworkInterfaces().stream().findFirst().orElse(null);

            if (nif == null) {
                log.warn("Интерфейс не найден или недоступен: {}", interfaceName);
                return;
            }

            handle = nif.openLive(bufferSize, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);

            log.info("Старт сниффера на интерфейсе: {}", nif.getName());

            new Thread(() -> {
                try {
                    handle.loop(-1, this::processPacket);
                } catch (Exception e) {
                    log.error("Ошибка захвата пакетов", e);
                }
            }).start();

        } catch (Exception e) {
            log.error("Ошибка запуска сниффера", e);
        }
    }

    /**
     * Прекращает захват трафика, если он активен.
     */
    public void stopCapture() {
        try {
            if (handle != null && handle.isOpen()) {
                handle.breakLoop();
                handle.close();
            }
        } catch (NotOpenException e) {
            log.error("Ошибка остановки сниффера", e);
        }

    }

    /**
     * Обрабатывает захваченный пакет.
     * Извлекает IP, порты, длину, протокол и при необходимости payload.
     *
     * @param packet {@link Packet} от Pcap4J
     */
    private void processPacket(Packet packet) {
        try {
            long timestamp = System.currentTimeMillis();

            IpPacket ipPacket = packet.get(IpPacket.class);
            if (ipPacket == null) return;

            InetAddress srcAddr = ipPacket.getHeader().getSrcAddr();
            InetAddress dstAddr = ipPacket.getHeader().getDstAddr();
            int srcPort = -1;
            int dstPort = -1;
            String protocol = "UNKNOWN";

            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcp = packet.get(TcpPacket.class);
                srcPort = tcp.getHeader().getSrcPort().valueAsInt();
                dstPort = tcp.getHeader().getDstPort().valueAsInt();
                protocol = "TCP";
            } else if (packet.contains(UdpPacket.class)) {
                UdpPacket udp = packet.get(UdpPacket.class);
                srcPort = udp.getHeader().getSrcPort().valueAsInt();
                dstPort = udp.getHeader().getDstPort().valueAsInt();
                protocol = "UDP";
            }

            int length = packet.length();
            String info = capturePayload ? packet.toString() : "";

            PacketDto packetDto = new PacketDto(
                    new Date(timestamp).toString(),
                    srcAddr.getHostAddress(),
                    dstAddr.getHostAddress(),
                    srcPort,
                    dstPort,
                    protocol,
                    length,
                    info
            );

            capturedPackets.add(packetDto);

            if (capturedPackets.size() > maxPacketsCapture) {
                capturedPackets.remove(0);
            }

            messagingTemplate.convertAndSend("/topic/packets", packetDto);

        } catch (Exception e) {
            log.error("Ошибка обработки пакета", e);
        }
    }

    /**
     * Прекращает захват при завершении работы приложения.
     */
    @PreDestroy
    public void onDestroy() {
        stopCapture();
    }

    /**
     * Удаляет текущий список пакетов и сбрасывает буфер.
     */
    public void clearCapture() {
        clearCapturedPackets();
        log.info("Буфер сниффера очищен");
    }
}
