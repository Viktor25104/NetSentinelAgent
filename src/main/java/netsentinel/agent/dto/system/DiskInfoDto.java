package netsentinel.agent.dto.system;

public record DiskInfoDto(
        String name,
        String mount,
        String type,
        long totalBytes,
        long usedBytes,
        long freeBytes,
        double usedPercent
) {}
