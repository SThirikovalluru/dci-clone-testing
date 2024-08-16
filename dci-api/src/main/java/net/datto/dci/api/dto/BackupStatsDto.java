package net.datto.dci.api.dto;

import lombok.Data;

@Data
public class BackupStatsDto {
	private Long activeServicesCount;
	private Long activeServicesWithRecentBackupCount;
	private String backupPercentage;
}