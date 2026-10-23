package com.alumniconnect.backend.dto.admin;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SetUserStatusRequest {
    @NotNull
    private Boolean active;
}
