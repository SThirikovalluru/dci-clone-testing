package net.datto.dci.api.dto;

import lombok.Data;

@Data
public class OffsiteStorageUsedDto {
	private long size;
	private String units;
}