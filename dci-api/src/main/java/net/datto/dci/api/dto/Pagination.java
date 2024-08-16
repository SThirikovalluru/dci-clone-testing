package net.datto.dci.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {
    private int page;
    private int perPage;
    private int totalPages;
    private int count;
}

