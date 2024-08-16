package net.datto.dciservice.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.datto.dci.api.dto.BackupStatsDto;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DCISaasDomain {
	private Long saasCustomerId;
	private String saasCustomerName = "";
	private Long seatsUsed;
	private String productType = "";
	private String retentionType = "";
	private String domain = "";
	private BackupStatsDto backupStats;


	public Long getSaasCustomerId() {
		return saasCustomerId;
	}

	@JsonSetter("saasCustomerId")
	public void setSaasCustomerId(Long saasCustomerId) {
		this.saasCustomerId = saasCustomerId;
	}

	public String getSaasCustomerName() {
		return saasCustomerName;
	}

	@JsonSetter("saasCustomerName")
	public void setSaasCustomerName(String saasCustomerName) {
		if (saasCustomerName.equalsIgnoreCase("") || saasCustomerName == null) {
			this.saasCustomerName =  "Unknown";
		} else {
			this.saasCustomerName = saasCustomerName;
		}
	}

	public Long getSeatsUsed() {
		return seatsUsed;
	}

	@JsonSetter("seatsUsed")
	public void setSeatsUsed(Long seatsUsed) {
		this.seatsUsed = seatsUsed;
	}

	public String getProductType() {
		return productType;
	}

	@JsonSetter("productType")
	public void setProductType(String productType) {
		this.productType = productType;
	}

	public String getRetentionType() {
		return retentionType;
	}

	@JsonSetter("retentionType")
	public void setRetentionType(String retentionType) {
		this.retentionType = retentionType;
	}

	public String getDomain() {
		return domain;
	}

	@JsonSetter("domain")
	public void setDomain(String domain) {
		this.domain = domain;
	}

	public BackupStatsDto getBackupStats() {
		return backupStats;
	}

	@JsonSetter("backupStats")
	public void setBackupStats(BackupStatsDto backupStats) {
		this.backupStats = backupStats;
	}

	@Override
	public String toString() {
		return "SaasDomain{" + "customerId='" + saasCustomerId + '\'' + ", customerName='" + saasCustomerName + '\'' + ", seatsUsed=" + seatsUsed + ", productType='" + productType + '\'' + ", retentionType='" + retentionType + '\'' + ", domain='" + domain + '\'' + '}';
	}
}