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
public class CantonDto  implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private  UUID id;
    private  String name;

    private List<ParroquiaDto> parroquias = new ArrayList<>();

    public CantonDto(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
