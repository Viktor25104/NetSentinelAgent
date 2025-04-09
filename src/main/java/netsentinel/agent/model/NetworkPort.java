package netsentinel.agent.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NetworkPort {
    private int port;
    private String protocol;
    private String service;
    private String state;
}

