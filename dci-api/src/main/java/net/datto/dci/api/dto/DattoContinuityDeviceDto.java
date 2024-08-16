package net.datto.dci.api.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DattoContinuityDeviceDto {
    private Action action;
    private String rmmAccountUid;
    private DattoContinuityDevice dciDevice;
}
