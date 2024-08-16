package net.datto.dci.api.dto.agent;

import lombok.Data;

@Data
public class ScreenshotVerificationDto {
    private String status;
    private String image;
}
