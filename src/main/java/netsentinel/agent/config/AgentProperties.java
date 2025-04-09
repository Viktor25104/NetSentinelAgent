package netsentinel.agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agent")
public record AgentProperties(
        String serverUrl,
        String name,
        String ip,
        String type,
        String location,
        long companyId
) {}
