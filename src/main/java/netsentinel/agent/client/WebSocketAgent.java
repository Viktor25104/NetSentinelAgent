package netsentinel.agent.client;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.function.Supplier;

/**
 * WebSocketAgent — главный агент клиента Net Sentinel, отвечающий за:
 * <ul>
 *   <li>установку WebSocket STOMP соединения с сервером</li>
 *   <li>автоматическую регистрацию сервера</li>
 *   <li>подписку на серверные каналы метрик</li>
 *   <li>ответ на запросы с актуальной информацией</li>
 *   <li>переподключение при потере связи</li>
 * </ul>
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class WebSocketAgent {

    // ============================ DEPENDENCIES ============================

    /**
     * Сервисы для сбора информации об ОС.
     */
    private final CpuService cpuService;
    private final DiskService diskService;
    private final NetworkService networkService;
    private final RamService ramService;
    private final ProcessService processService;
    private final NetworkPortMonitoringService networkPortMonitoringService;
    private final StartupService startupService;

    /** STOMP WebSocket клиент */
    private final WebSocketStompClient stompClient;

    /** Настройки агента из application.yml */
    private final AgentProperties props;

    /** JSON-сериализатор */
    private final ObjectMapper mapper = new ObjectMapper();

    /** STOMP сессия */
    private StompSession session;

    /** Текущий sessionId, выданный сервером */
    private String sessionId;

    /** Подписки, чтобы избежать повторов */
    private final Set<String> subscribedTopics = new HashSet<>();

    /** Файл, в который сохраняется sessionId между запусками */
    private static final Path SESSION_FILE = Path.of("session_id.txt");

    // ============================ LIFECYCLE ============================

    /**
     * Инициализация WebSocket соединения при старте приложения.
     * Подключает STOMP-клиент и запускает подписку.
     */
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

    // ============================ SUBSCRIBE ============================

    /**
     * Подписка на получение sessionId от сервера (один раз при старте).
     */
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

    /**
     * Подписка на все топики метрик сервера, если соединение активно.
     */
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
                Thread.sleep(50); // защита от TEXT_PARTIAL_WRITING
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

    // ============================ SEND ============================

    /**
     * Отправляет ответ на сервер по /app/response
     *
     * @param type    тип метрики (cpu, ram, ports и т.д.)
     * @param payload объект с данными (DTO)
     */
    private void sendResponse(String type, Object payload) {
        if (session != null && session.isConnected()) {
            try {
                var response = new AgentResponse(sessionId, type, payload);
                session.send("/app/response", response);
            } catch (Exception e) {
                System.err.println("❌ Ошибка отправки ответа: " + e.getMessage());
            }
        }
    }

    /**
     * Отправляет базовую информацию о сервере при подключении
     */
    private void sendServerInfo() {
        sendResponse("register", buildServerInfo());
    }

    /**
     * Сбор ServerInfoDto с основных метрик (используется для регистрации).
     *
     * @return DTO с информацией о сервере
     */
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

    /**
     * Периодическая отправка метрик сервера (CPU, RAM, Disk и др.) на сервер
     * без запроса с его стороны. Используется для поддержания статуса "online"
     * и регулярного обновления данных.
     * <p>
     * Отправляет объект {@link ServerInfoDto} в обёртке {@link AgentResponse}
     * на канал "/app/response" каждые 10 секунд, если WebSocket-сессия активна.
     * <p>
     * Используется совместно с {@link netsentinel.agent.dto.agent.AgentResponse}
     * и {@link org.springframework.scheduling.annotation.Scheduled}
     */
    @Scheduled(fixedRate = 10_000)
    public void sendPeriodicInfoToServer() {
        if (session == null || !session.isConnected()) {
            System.out.println("❌ WebSocket не подключён, пропуск отправки метрик.");
            return;
        }

        if (!isValidSessionId(sessionId)) {
            System.out.println("❌ sessionId отсутствует — агент не зарегистрирован.");
            return;
        }

        try {
            ServerInfoDto info = buildServerInfo(); // уже готовый метод
            AgentResponse response = new AgentResponse(sessionId, "info", info);

            session.send("/app/response", response);
            System.out.println("📤 Агент отправил метрики (info): " + response);
        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки info: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // ============================ UTILS ============================

    /**
     * Запускает reconnect в отдельном потоке с экспоненциальной задержкой.
     */
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

    /**
     * Сохраняет sessionId в файл.
     *
     * @param sessionId session UUID
     */
    private void saveSessionId(String sessionId) {
        try {
            Files.writeString(SESSION_FILE, sessionId, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения sessionId: " + e.getMessage());
        }
    }

    /**
     * Загружает sessionId из файла, если существует.
     *
     * @return строка или null
     */
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

    /**
     * Проверяет, является ли sessionId валидным.
     *
     * @param id строка ID
     * @return true, если id не пустой и не null
     */
    private boolean isValidSessionId(String id) {
        return id != null && !id.isBlank() && !"null".equalsIgnoreCase(id);
    }
}
