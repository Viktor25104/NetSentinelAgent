package netsentinel.agent.dto.agent;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ServerInfoDto(
        @JsonProperty("sessionId") String sessionId,
        @JsonProperty("name") String name,
        @JsonProperty("ip") String ip,
        @JsonProperty("type") String type,
        @JsonProperty("status") String status,
        @JsonProperty("location") String location,
        @JsonProperty("uptime") long uptime,
        @JsonProperty("cpuUsage") int cpuUsage,
        @JsonProperty("memoryUsage") int memoryUsage,
        @JsonProperty("diskUsage") int diskUsage,
        @JsonProperty("companyId") long companyId
) {}
