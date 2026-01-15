package com.hoops.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RejectParticipationRequest(
        @NotBlank(message = "거절 사유는 필수입니다")
        String reason
) {
}
