package com.kynsof.identity.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProvinceDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  UUID id;
    private  String name;

    // Agregamos la lista de cantones
    private List<CantonDto> cantones = new ArrayList<>();

    public ProvinceDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
