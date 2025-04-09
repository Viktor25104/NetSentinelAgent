package netsentinel.agent.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import netsentinel.agent.config.AgentProperties;
import netsentinel.agent.dto.agent.AgentResponse;
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
import java.util.*;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class WebSocketAgent {

    private final CpuService cpuService;
    private final DiskService diskService;
    private final NetworkService networkService;
    private final RamService ramService;
    private final ProcessService processService;
    private final NetworkPortMonitoringService networkPortMonitoringService;
    private final StartupService startupService;
    private final WebSocketStompClient stompClient;
    private final AgentProperties props;

    private final ObjectMapper mapper = new ObjectMapper();

    private StompSession session;
    private String sessionId;
    private final Set<String> subscribedTopics = new HashSet<>();
    private static final Path SESSION_FILE = Path.of("session_id.txt");

    @PostConstruct
    public void connect() {
        try {
            sessionId = loadSessionId();
            stompClient.connectAsync(props.serverUrl(), new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    WebSocketAgent.this.session = session;
                    System.out.println("✅ WebSocket подключён");

                    if (isValidSessionId(sessionId)) {
                        subscribeToTopics();
                    }

                    sendServerInfo();
                    subscribeToSessionChannel();
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    System.err.println("❌ WebSocket ошибка транспорта: " + exception.getMessage());
                    reconnectAsync();
                }
            }).exceptionally(ex -> {
                System.err.println("❌ Ошибка подключения WebSocket: " + ex.getMessage());
                reconnectAsync();
                return null;
            });
        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации агента: " + e.getMessage());
        }
    }

    private void subscribeToSessionChannel() {
        String sessionChannel = "/queue/session/" + props.ip();
        session.subscribe(sessionChannel, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                sessionId = payload.toString();
                saveSessionId(sessionId);

                if (isValidSessionId(sessionId)) {
                    subscribeToTopics();
                } else {
                    System.err.println("⚠️ Невалидный sessionId");
                }
            }
        });
    }

    private void subscribeToTopics() {
        if (session == null || !session.isConnected()) {
            System.err.println("❌ Сессия WebSocket неактивна!");
            return;
        }

        if (!isValidSessionId(sessionId)) {
            System.err.println("❌ sessionId отсутствует! Подписка невозможна.");
            return;
        }

        Map<String, Supplier<?>> subscriptions = Map.of(
                "info", this::buildServerInfo,
                "cpu", cpuService::getCpuInfo,
                "ram", ramService::getRamInfo,
                "disk", diskService::getDisksInfo,
                "network", networkService::getNetworkInfo,
                "process", processService::getProcessList,
                "ports", networkPortMonitoringService::getPorts,
                "startup", startupService::getStartupList
        );

        subscriptions.forEach((type, supplier) -> {
            String topic = "/topic/server/" + sessionId + "/" + type;

            if (!subscribedTopics.add(topic)) {
                System.out.println("⚠️ Уже подписан на " + topic);
                return;
            }

            try {
                Thread.sleep(50);
                session.subscribe(topic, new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return String.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        sendResponse(type, supplier.get());
                    }
                });
                System.out.println("✅ Подписка на " + topic + " выполнена");
            } catch (Exception e) {
                System.err.println("❌ Ошибка подписки на " + topic + ": " + e.getMessage());
            }
        });
    }

    private void sendResponse(String type, Object payload) {
        if (session != null && session.isConnected()) {
            try {
                var response = new AgentResponse(sessionId, type, payload);
                session.send("/app/response", mapper.writeValueAsString(response));
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки ответа: " + e.getMessage());
            }
        }
    }

    private void sendServerInfo() {
        sendResponse("register", buildServerInfo());
    }

    private ServerInfoDto buildServerInfo() {
        return new ServerInfoDto(
                sessionId,
                props.name(),
                props.ip(),
                props.type(),
                "online",
                props.location(),
                24 * 60 * 60,
                (int) cpuService.getCpuLoad(),
                (int) ramService.getRamLoad(),
                diskService.getOverallDiskLoad(),
                props.companyId()
        );
    }

    private void reconnectAsync() {
        new Thread(() -> {
            int delay = 5000;
            for (int attempt = 1; attempt <= 5; attempt++) {
                try {
                    System.out.println("🔁 Повторное подключение через " + delay / 1000 + "с (попытка " + attempt + ")");
                    Thread.sleep(delay);
                    connect();
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                delay *= 2;
            }
            System.err.println("❌ Не удалось переподключиться после нескольких попыток.");
        }).start();
    }

    private void saveSessionId(String sessionId) {
        try {
            Files.writeString(SESSION_FILE, sessionId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения sessionId: " + e.getMessage());
        }
    }

    private String loadSessionId() {
        try {
            if (Files.exists(SESSION_FILE)) {
                return Files.readString(SESSION_FILE).trim();
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка загрузки sessionId: " + e.getMessage());
        }
        return null;
    }

    private boolean isValidSessionId(String id) {
        return id != null && !id.isBlank() && !"null".equalsIgnoreCase(id);
    }
}