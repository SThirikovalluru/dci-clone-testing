package net.datto.dci.api.dto.agent;

import lombok.Data;

@Data
public class BackupDto {
    private String timestamp;
    private BackupObjectDto backup;
    private LocalVerificationDto localVerification;
    private AdvancedVerificationDto advancedVerification;
}
