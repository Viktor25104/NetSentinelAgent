package netsentinel.agent.dto.network;

public record NetworkPortDto(
        int port,
        String protocol,
        String service,
        String state
) {}
