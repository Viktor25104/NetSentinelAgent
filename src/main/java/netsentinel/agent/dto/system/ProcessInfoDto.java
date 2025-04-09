package netsentinel.agent.dto.system;

public record ProcessInfoDto(
        int pid,
        String name,
        String sessionName, // только для Windows
        Integer sessionId,   // Windows
        String memoryUsage,  // Windows (например, "12,000 K")
        Double mem,          // Linux
        Double cpu           // Linux
) {}
