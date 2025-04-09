package netsentinel.agent.dto.system;

public record StartupItemDto(
        String name,
        String location,
        boolean enabled
) {}
