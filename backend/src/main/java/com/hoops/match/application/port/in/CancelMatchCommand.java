package com.hoops.match.application.port.in;

public record CancelMatchCommand(
        Long matchId,
        Long userId,
        String reason
) {
}
