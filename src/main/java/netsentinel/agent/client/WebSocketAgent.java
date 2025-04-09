package netsentinel.agent.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import netsentinel.agent.dto.agent.ServerInfoDto;
import netsentinel.agent.service.network.NetworkPortMonitoringService;
import netsentinel.agent.service.network.NetworkService;
import netsentinel.agent.service.system.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Supplier;

@Component
public class WebSocketAgent {

    private final CpuService cpuService;
    private final DiskService diskService;
    private final NetworkService networkService;
    private final RamService ramService;
    private final ProcessService processService;
    private final NetworkPortMonitoringService networkPortMonitoringService;
    private final StartupService startupService;
    private final WebSocketStompClient stompClient;

    private StompSession session;
    private final ObjectMapper mapper = new ObjectMapper();

    private String sessionId;
    private final String serverUrl = "ws://localhost:8080/ws";
    private final String name = "Server-01";
    private final String ip = "192.168.1.150";
    private final String type = "Linux";
    private final String location = "DataCenter-1";
    private final long companyId = 1L;

    public WebSocketAgent(CpuService cpuService, DiskService diskService,
                          NetworkService networkService, RamService ramService,
                          ProcessService processService,
                          NetworkPortMonitoringService networkPortMonitoringService,
                          StartupService startupService, WebSocketStompClient stompClient) {
        this.cpuService = cpuService;
        this.diskService = diskService;
        this.networkService = networkService;
        this.ramService = ramService;
        this.processService = processService;
        this.networkPortMonitoringService = networkPortMonitoringService;
        this.startupService = startupService;
        this.stompClient = stompClient;
    }

    @PostConstruct
    public void connect() {
        try {
            sessionId = loadSessionId();

            stompClient.connectAsync(serverUrl, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    WebSocketAgent.this.session = session;
                    System.out.println("✅ WebSocket подключён");

                    if (isValidSessionId(sessionId)) {
                        System.out.println("📌 Используется sessionId: " + sessionId);
                        subscribeToServerTopics();
                    }

                    sendServerInfo();
                    subscribeToSessionChannel();
                }

                @Override
                public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                    System.err.println("❌ Ошибка STOMP: " + command);
                    exception.printStackTrace();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    System.err.println("❌ Транспортная ошибка WebSocket: " + exception.getMessage());
                    exception.printStackTrace();
                    reconnectAsync();
                }
            }).exceptionally(ex -> {
                System.err.println("❌ Ошибка подключения WebSocket: " + ex.getMessage());
                ex.printStackTrace();
                reconnectAsync();
                return null;
            });

        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации агента: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void subscribeToSessionChannel() {
        String sessionChannel = "/queue/session/" + ip;
        System.out.println("🔗 Подписка на получение sessionId: " + sessionChannel);

        session.subscribe(sessionChannel, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                System.out.println("📩 Получено сообщение в sessionChannel: " + payload);

                try {
                    sessionId = payload.toString();
                    saveSessionId(sessionId);
                    System.out.println("✅ sessionId сохранён: " + sessionId);

                    if (!isValidSessionId(sessionId)) {
                        System.err.println("⚠️ Невалидный sessionId");
                        return;
                    }

                    subscribeToServerTopics();
                } catch (Exception e) {
                    System.err.println("❌ Ошибка обработки sessionId: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        System.out.println("🕒 Ожидание sessionId от сервера...");
    }

    private void sendServerInfo() {
        if (session != null && session.isConnected()) {
            try {
                long uptimeInSeconds = 24 * 60 * 60;
                int cpuUsage = (int) cpuService.getCpuLoad();
                int memoryUsage = extractInt(ramService.getRamInfo());
                int diskUsage = extractInt(diskService.getDisksInfo());

                ServerInfoDto serverInfo = new ServerInfoDto(
                        sessionId, name, ip, type, "online", location, uptimeInSeconds,
                        cpuUsage, memoryUsage, diskUsage, companyId
                );

                String json = mapper.writeValueAsString(serverInfo);
                System.out.println("📤 Отправка JSON: " + json);

                session.send("/app/register", serverInfo);
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки информации о сервере: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.err.println("⚠️ Сессия WebSocket не активна");
        }
    }

    private void subscribeToServerTopics() {
        if (session == null || !session.isConnected()) {
            System.err.println("❌ WebSocket сессия не активна!");
            return;
        }

        if (!isValidSessionId(sessionId)) {
            System.err.println("❌ sessionId отсутствует! Подписка невозможна.");
            return;
        }

        System.out.println("📡 Подписка на WebSocket-каналы для sessionId: " + sessionId);

        subscribe("/topic/server/" + sessionId + "/info", () -> {
            ServerInfoDto serverInfo = new ServerInfoDto(
                    sessionId, name, ip, type, "online", location, 0L,
                    (int) cpuService.getCpuLoad(),
                    (int) ramService.getRamLoad(),
                    diskService.getOverallDiskLoad(),
                    companyId
            );
            try {
                return mapper.writeValueAsString(serverInfo);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return "{}";
            }
        });

        subscribe("/topic/server/" + sessionId + "/cpu", cpuService::getCpuInfo);
        subscribe("/topic/server/" + sessionId + "/disk", diskService::getDisksInfo);
        subscribe("/topic/server/" + sessionId + "/network", networkService::getNetworkInfo);
        subscribe("/topic/server/" + sessionId + "/ram", ramService::getRamInfo);
        subscribe("/topic/server/" + sessionId + "/process", processService::getProcessList);
        subscribe("/topic/server/" + sessionId + "/ports", networkPortMonitoringService::getPorts);
        subscribe("/topic/server/" + sessionId + "/startup", startupService::getStartupList);
    }

    private <T> void subscribe(String topic, Supplier<T> dataSupplier) {
        try {
            session.subscribe(topic, new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class;
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    try {
                        System.out.println("📩 Запрос на " + topic + ": " + payload);
                        T data = dataSupplier.get();
                        String dataType = topic.substring(topic.lastIndexOf("/") + 1);
                        String response = sessionId + ":" + dataType + ":" + data;
                        session.send("/app/response", response);
                        System.out.println("📤 Ответ на " + topic + ": " + response);
                    } catch (Exception e) {
                        System.err.println("❌ Ошибка ответа на " + topic + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("✅ Подписка на " + topic + " выполнена");
        } catch (Exception e) {
            System.err.println("❌ Ошибка подписки на " + topic + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSessionId(String sessionId) {
        try {
            Path path = Path.of("session_id.txt");
            Files.writeString(path, sessionId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("💾 sessionId сохранён в " + path.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения sessionId: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String loadSessionId() {
        try {
            Path path = Path.of("session_id.txt");
            if (Files.exists(path)) {
                String id = Files.readString(path).trim();
                System.out.println("🔄 sessionId загружен: " + id);
                return id;
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки sessionId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private boolean isValidSessionId(String id) {
        return id != null && !id.isEmpty() && !"null".equalsIgnoreCase(id);
    }

    private void reconnectAsync() {
        try {
            Thread.sleep(5000);
            connect();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int extractInt(Object data) {
        if (data == null) return 0;

        if (data instanceof Number number) return number.intValue();

        if (data instanceof String str) {
            str = str.trim();
            if (str.equalsIgnoreCase("Н/Д") || str.equalsIgnoreCase("N/A")) return 0;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                System.err.println("❌ Ошибка преобразования в число: " + str);
            }
        }

        return 0;
    }
}
