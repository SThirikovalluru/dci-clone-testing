package net.datto.dci.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SaasDomain {
	@JsonProperty("saasCustomerId")
	private Long customerId;
	@JsonProperty("saasCustomerName")
	private String customerName = "Unknown";
	private Long seatsUsed;
	private String productType = "";
	private String retentionType = "";
	private String domain = "";
	//backup Stats
	private BackupStatsDto backupStats;
}