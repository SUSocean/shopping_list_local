package com.SUSocean.Shopping_List.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {
    private UUID id;

    private String name;

    private Integer position;

    private Boolean active;
}
