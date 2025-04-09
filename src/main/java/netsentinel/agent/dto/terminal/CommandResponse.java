package netsentinel.agent.dto.terminal;

public record CommandResponse(
        String output,
        String error,
        boolean success
) {}
