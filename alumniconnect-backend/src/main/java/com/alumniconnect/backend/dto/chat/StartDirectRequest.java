package com.alumniconnect.backend.dto.chat;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartDirectRequest {
    @NotNull(message = "Choose who to message")
    private Long userId;
}
