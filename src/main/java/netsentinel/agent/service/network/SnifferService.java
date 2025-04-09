package netsentinel.agent.service.network;

import lombok.extern.slf4j.Slf4j;
import netsentinel.agent.dto.network.PacketDto;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Slf4j
public class SnifferService {

    private final SimpMessagingTemplate messagingTemplate;
    private PcapHandle handle;
    private ExecutorService executor;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final List<PacketDto> capturedPackets = new ArrayList<>();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public SnifferService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public List<String> getNetworkInterfaces() {
        List<String> interfaces = new ArrayList<>();
        try {
            for (PcapNetworkInterface nif : Pcaps.findAllDevs()) {
                interfaces.add(nif.getName() + " - " + nif.getDescription());
            }
        } catch (PcapNativeException e) {
            log.error("Error getting network interfaces", e);
        }
        return interfaces;
    }

    public List<PacketDto> getCapturedPackets() {
        return new ArrayList<>(capturedPackets);
    }

    public void startCapture(String interfaceName) {
        if (isRunning.get()) {
            log.info("Capture is already running");
            return;
        }

        try {
            log.info("Starting capture with interface: {}", interfaceName);

            List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
            log.info("Found {} network interfaces", allDevs.size());

            // Логируем все доступные интерфейсы для отладки
            for (PcapNetworkInterface dev : allDevs) {
                log.info("Available interface: {} ({})", dev.getName(),
                        dev.getDescription() != null ? dev.getDescription() : "No description");
            }

            PcapNetworkInterface nif = null;

            // Если интерфейс указан явно, пытаемся его найти
            if (interfaceName != null && !interfaceName.isEmpty()) {
                // Обрабатываем возможные варианты имени интерфейса
                String normalizedName = interfaceName.replace("/", "\\");

                for (PcapNetworkInterface dev : allDevs) {
                    // Проверяем точное соответствие
                    if (dev.getName().equals(normalizedName) ||
                            dev.getName().equals(interfaceName)) {
                        nif = dev;
                        log.info("Found exact matching interface: {}", dev.getName());
                        break;
                    }

                    // Если не нашли точное соответствие, ищем частичное
                    if (nif == null && dev.getName().contains(normalizedName) ||
                            (normalizedName.contains("{") && dev.getName().contains(normalizedName.substring(normalizedName.indexOf("{"))))) {
                        nif = dev;
                        log.info("Found partial matching interface: {}", dev.getName());
                        // Не прерываем цикл, возможно найдется точное соответствие
                    }
                }
            }

            // Если интерфейс не найден или не указан, используем первый доступный
            if (nif == null) {
                if (!allDevs.isEmpty()) {
                    nif = allDevs.get(0);
                    log.info("Using default interface: {}", nif.getName());
                } else {
                    log.error("No network interfaces available");
                    return;
                }
            }

            // Открываем интерфейс для захвата
            log.info("Opening interface for capture: {}", nif.getName());
            handle = nif.openLive(65536, PcapNetworkInterface.PromiscuousMode.PROMISCUOUS, 10);
            isRunning.set(true);

            // Создаем поток для захвата пакетов
            executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                int packetCount = 0;
                while (isRunning.get()) {
                    try {
                        Packet packet = handle.getNextPacket();
                        if (packet != null) {
                            processPacket(packet);
                            packetCount++;

                            // Периодически логируем статистику
                            if (packetCount % 100 == 0) {
                                log.info("Captured {} packets so far", packetCount);
                            }
                        }
                    } catch (NotOpenException e) {
                        if (isRunning.get()) {
                            log.error("Error capturing packet", e);
                        }
                        break;
                    }
                }
                log.info("Packet capture thread finished, total packets: {}", packetCount);
            });

            log.info("Packet capturing started on interface: {}", nif.getName());
        } catch (PcapNativeException e) {
            log.error("Pcap native error starting capture", e);
        } catch (Exception e) {
            log.error("Unexpected error starting packet capture", e);
        }
    }

    public void stopCapture() {
        if (!isRunning.get()) {
            return;
        }

        isRunning.set(false);
        if (handle != null) {
            handle.close();
        }
        if (executor != null) {
            executor.shutdown();
        }
        log.info("Packet capturing stopped");
    }

    public void clearCapture() {
        capturedPackets.clear();
        messagingTemplate.convertAndSend("/topic/packets", capturedPackets);
        log.info("Packet capture cleared");
    }

    private void processPacket(Packet packet) {
        try {
            PacketDto packetDto = new PacketDto();
            packetDto.setTimestamp(LocalDateTime.now().format(formatter));
            packetDto.setLength(packet.length());

            // Process Ethernet layer
            if (packet.contains(EthernetPacket.class)) {
                EthernetPacket ethernetPacket = packet.get(EthernetPacket.class);
                packetDto.setInfo("Ethernet: " + ethernetPacket.getHeader().getSrcAddr() + " -> " +
                        ethernetPacket.getHeader().getDstAddr());
            }

            // Process IP layer
            if (packet.contains(IpPacket.class)) {
                IpPacket ipPacket = packet.get(IpPacket.class);
                packetDto.setSourceIP(ipPacket.getHeader().getSrcAddr().getHostAddress());
                packetDto.setDestinationIP(ipPacket.getHeader().getDstAddr().getHostAddress());
                packetDto.setProtocol(String.valueOf(ipPacket.getHeader().getProtocol()));
            }

            // Process TCP layer
            if (packet.contains(TcpPacket.class)) {
                TcpPacket tcpPacket = packet.get(TcpPacket.class);
                packetDto.setSourcePort(tcpPacket.getHeader().getSrcPort().valueAsInt());
                packetDto.setDestinationPort(tcpPacket.getHeader().getDstPort().valueAsInt());
                packetDto.setProtocol("TCP");
                packetDto.setInfo("TCP: " + packetDto.getSourceIP() + ":" + packetDto.getSourcePort() +
                        " -> " + packetDto.getDestinationIP() + ":" + packetDto.getDestinationPort());
            }

            // Process UDP layer
            if (packet.contains(UdpPacket.class)) {
                UdpPacket udpPacket = packet.get(UdpPacket.class);
                packetDto.setSourcePort(udpPacket.getHeader().getSrcPort().valueAsInt());
                packetDto.setDestinationPort(udpPacket.getHeader().getDstPort().valueAsInt());
                packetDto.setProtocol("UDP");
                packetDto.setInfo("UDP: " + packetDto.getSourceIP() + ":" + packetDto.getSourcePort() +
                        " -> " + packetDto.getDestinationIP() + ":" + packetDto.getDestinationPort());
            }

            capturedPackets.add(packetDto);
            messagingTemplate.convertAndSend("/topic/packets", capturedPackets);
        } catch (Exception e) {
            log.error("Error processing packet", e);
        }
    }

}
