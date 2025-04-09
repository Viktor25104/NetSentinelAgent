package netsentinel.agent.dto.agent;

public record AgentResponse(
        String sessionId,
        String type,
        Object payload
) {
}
