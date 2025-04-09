package netsentinel.agent.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "sysmonitor")
@RefreshScope
public class MonitoringConfig {

    private Polling polling = new Polling();
    private Network network = new Network();
    private Terminal terminal = new Terminal();

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

    @Getter
    @Setter
    public static class Network {
        private int maxPacketsCapture;
        private int packetBufferSize;
        private boolean capturePayload;
    }

    @Getter
    @Setter
    public static class Terminal {
        private int historySize;
        private int timeout;
    }
}
