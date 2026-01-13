package com.hoops.match.application.port.in;

public record ReactivateMatchCommand(
        Long matchId,
        Long userId
) {
}
