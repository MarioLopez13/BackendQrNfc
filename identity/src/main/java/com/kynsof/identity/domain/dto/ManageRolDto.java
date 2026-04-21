package com.kynsof.identity.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ManageRolDto {
    private UUID id;
    private String code;
    private String name;
    private Boolean isDeleted;
}
