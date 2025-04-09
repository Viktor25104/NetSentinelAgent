package netsentinel.agent.dto.network;

public record NetworkInterfaceDto(
        String name,
        String displayName,
        String mac,
        String[] ipv4,
        String[] ipv6,
        long bytesSent,
        long bytesRecv,
        long speed
) {}
