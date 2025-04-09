package netsentinel.agent.dto.system;

public record RamInfoDto(
        long totalBytes,
        long usedBytes,
        long freeBytes,
        double usedPercent
) {}
