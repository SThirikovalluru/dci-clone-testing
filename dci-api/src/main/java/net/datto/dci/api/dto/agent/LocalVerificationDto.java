package net.datto.dci.api.dto.agent;

import lombok.Data;

import java.util.List;

@Data
public class LocalVerificationDto {
    private String status;
    private List<ErrorDto> errors;
}
