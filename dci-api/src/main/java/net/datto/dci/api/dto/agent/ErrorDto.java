package net.datto.dci.api.dto.agent;

import lombok.Data;

@Data
public class ErrorDto {
    private String errorType;
    private String errorMessage;
}
