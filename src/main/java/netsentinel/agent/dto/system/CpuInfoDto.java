package netsentinel.agent.dto.system;

public record CpuInfoDto(
        String name,
        int physicalCores,
        int logicalCores,
        double load
) {}
