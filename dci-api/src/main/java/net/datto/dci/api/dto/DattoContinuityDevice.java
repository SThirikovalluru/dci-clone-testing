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
public class DattoContinuityDevice {
	private String serialNumber = "";
	private String name = "Unknown";
	private String model = "";
	@JsonProperty("internalIP")
	private String internalIpAddress = "";
	private String clientCompanyName = "";
	private String rmmSiteUid;
	private String lastSeenDate;
	@JsonProperty("uptime")
	private int upTime;
	private String remoteWebUrl;
	private String warrantyExpire;
	private LocalStorageUsedDto localStorageUsed;
	private LocalStorageAvailableDto localStorageAvailable;
	private OffsiteStorageUsedDto offsiteStorageUsed;
}