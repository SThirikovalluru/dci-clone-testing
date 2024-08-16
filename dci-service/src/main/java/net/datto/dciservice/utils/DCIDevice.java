package net.datto.dciservice.utils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import net.datto.dci.api.dto.LocalStorageAvailableDto;
import net.datto.dci.api.dto.LocalStorageUsedDto;
import net.datto.dci.api.dto.OffsiteStorageUsedDto;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DCIDevice {
	private String serialNumber = "";
	private String name = "";
	private String model = "";
	private String internalIpAddress = "";
	private String clientCompanyName = "";
	private String rmmSiteUid;
	private String lastSeenDate;
	private int upTime;
	private String remoteWebUrl;
	private String warrantyExpire;
	private LocalStorageUsedDto localStorageUsed;
	private LocalStorageAvailableDto localStorageAvailableDto;
	private OffsiteStorageUsedDto offsiteStorageUsedDto;
	 
	public String getSerialNumber() {
		return serialNumber;
	}

	@JsonSetter("serialNumber")
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getName() {
		return name;
	}

	@JsonSetter("name")
	public void setName(String name) {
		if (name.equalsIgnoreCase("") || name == null) {
			this.name =  "Unknown";
		} else {
			this.name = name;
		}
	}

	public String getModel() {
		return model;
	}

	@JsonSetter("model")
	public void setModel(String model) {
		this.model = model;
	}

	public String getInternalIpAddress() {
		return internalIpAddress;
	}

	@JsonSetter("internalIP")
	public void setInternalIpAddress(String internalIpAddress) {
		this.internalIpAddress = internalIpAddress;
	}

	public String getClientCompanyName() {
		return clientCompanyName;
	}
	
	@JsonSetter("clientCompanyName")
	public void setClientCompanyName(String clientCompanyName) {
		this.clientCompanyName = clientCompanyName;
	}

	public String getRmmSiteUid() {
		return rmmSiteUid;
	}

	public void setRmmSiteUid(String rmmSiteUid) {
		this.rmmSiteUid = rmmSiteUid;
	}

	public String getLastSeenDate() {
		return lastSeenDate;
	}
	
	@JsonSetter("lastSeenDate")
	public void setLastSeenDate(String lastSeenDate) {
		this.lastSeenDate = lastSeenDate;
	}
	
	public int getUpTime() {
		return upTime;
	}

	@JsonSetter("uptime")
	public void setUpTime(int upTime) {
		this.upTime = upTime;
	}

	public String getRemoteWebUrl() {
		return remoteWebUrl;
	}

	@JsonSetter("remoteWebUrl")
	public void setRemoteWebUrl(String remoteWebUrl) {
		this.remoteWebUrl = remoteWebUrl;
	}
	
	public String getWarrantyExpire() {
		return warrantyExpire;
	}

	@JsonSetter("warrantyExpire")
	public void setWarrantyExpire(String warrantyExpire) {
		this.warrantyExpire = warrantyExpire;
	}
	
	public LocalStorageUsedDto getLocalStorageUsed() {
		return localStorageUsed;
	}

	@JsonSetter("localStorageUsed")
	public void setLocalStorageUsed(LocalStorageUsedDto localStorageUsed) {
		this.localStorageUsed = localStorageUsed;
	}
	
	public LocalStorageAvailableDto getLocalStorageAvailableDto() {
		return localStorageAvailableDto;
	}

	@JsonSetter("localStorageAvailable")
	public void setLocalStorageAvailableDto(LocalStorageAvailableDto localStorageAvailableDto) {
		this.localStorageAvailableDto = localStorageAvailableDto;
	}

	public OffsiteStorageUsedDto getOffsiteStorageUsed() {
		return offsiteStorageUsedDto;
	}

	@JsonDeserialize(using = StorageHandler.class)
	@JsonSetter("offsiteStorageUsed")
	public void setOffsiteStorageUsed(OffsiteStorageUsedDto offsiteStorageUsedDto) {
		this.offsiteStorageUsedDto = offsiteStorageUsedDto;
	}

	@Override
	public String toString() {
		return "DattoContinuityDevice{" +
				"serialNumber='" + serialNumber + '\'' +
				", name='" + name + '\'' +
				", model='" + model + '\'' +
				", internalIpAddress='" + internalIpAddress + '\'' +
				", clientCompanyName='" + clientCompanyName + '\'' +
				", lastSeenDate='" + lastSeenDate + '\'' +
				", rmmSiteUid='" + rmmSiteUid + '\'' +
				", upTime='" + upTime + '\'' +
				", remoteWebUrl='" + remoteWebUrl + '\'' +
				", warrantyExpire='" + warrantyExpire + '\'' +
				", localStorageUsed='" + localStorageUsed + '\'' +
				", localStorageAvailable='" + localStorageUsed + '\'' +
				", offsiteStorageUsed='" + offsiteStorageUsedDto + '\'' +
				'}';
	}

	

}