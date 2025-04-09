package netsentinel.agent.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServerInfo implements Serializable {

    @JsonProperty("sessionId")
    private String sessionId;  // Уникальный ID сессии

    @JsonProperty("name")
    private String name;       // Имя сервера

    @JsonProperty("ip")
    private String ip;         // IP-адрес

    @JsonProperty("type")
    private String type;       // Операционная система (Linux, Windows)

    @JsonProperty("status")
    private String status;     // online | offline | maintenance

    @JsonProperty("location")
    private String location;   // Где находится сервер

    @JsonProperty("uptime")
    private long uptime;       // Время работы сервера (в секундах)

    @JsonProperty("cpuUsage")
    private int cpuUsage;      // Нагрузка на CPU

    @JsonProperty("memoryUsage")
    private int memoryUsage;   // Нагрузка на RAM

    @JsonProperty("diskUsage")
    private int diskUsage;     // Нагрузка на диск

    @JsonProperty("companyId")
    private long companyId;    // ID компании
}
