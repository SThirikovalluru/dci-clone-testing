package net.datto.dci.api.dto;

import lombok.Data;

@Data
public class LocalStorageUsedDto {
	private long size;
	private String units;
}