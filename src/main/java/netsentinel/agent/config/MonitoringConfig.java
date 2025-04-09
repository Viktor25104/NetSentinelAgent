package netsentinel.agent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация мониторинга, изменяемая в рантайме.
 * <p>
 * Включает настройки опроса, сетевого сниффера и терминала.
 * Поддерживает {@code /actuator/refresh}.
 *
 * @author Viktor Marymorych
 * @since 1.0
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "sysmonitor")
@RefreshScope
public class MonitoringConfig {

    /** Настройки частоты опроса метрик. */
    private Polling polling = new Polling();

    /** Настройки сниффера пакетов. */
    private Network network = new Network();

    /** Настройки терминального доступа. */
    private Terminal terminal = new Terminal();

    /**
     * Конфигурация периодов опроса различных метрик.
     */
    @Getter
    @Setter
    public static class Polling {
        private long cpu;
        private long ram;
        private long disk;
        private long network;
        private long processes;
        private long startup;
        private long ports;
    }

    /**
     * Параметры захвата сетевых пакетов.
     */
    @Getter
    @Setter
    public static class Network {
        private int maxPacketsCapture;
        private int packetBufferSize;
        private boolean capturePayload;
    }

    /**
     * Ограничения и параметры терминала.
     */
    @Getter
    @Setter
    public static class Terminal {
        private int historySize;
        private int timeout;
    }
}
