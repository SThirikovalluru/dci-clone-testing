package net.datto.dci.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceResponse<T> {
    private Pagination pagination;
    private List<T> items;
}

