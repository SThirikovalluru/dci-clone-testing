package net.datto.dci.api.dto.agent;

import lombok.Data;

@Data
public class BackupObjectDto {
    private String status;
    private String errorMessage;
}
