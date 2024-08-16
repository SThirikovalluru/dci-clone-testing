package net.datto.dci.api.dto.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentDto {
    private String name;
    private String volume;
    private String localIp;
    private Long latestOffsite;
    private Long lastScreenshotAttempt;
    private String lastScreenshotAttemptStatus;
    private Long localSnapshots;
    private Long lastSnapshot;
    private String agentVersion;
    private int protectedVolumesCount;
    private int unprotectedVolumesCount;
    private Boolean isArchived;
    private Boolean isPaused;
    private List<String> protectedVolumeNames;
    private List<String> unprotectedVolumeNames;
    private List<BackupDto> backups;

}
