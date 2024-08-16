package net.datto.dci.api.dto;


import lombok.Data;

@Data
public class LocalStorageAvailableDto {
	private long size;
	private String units;
}