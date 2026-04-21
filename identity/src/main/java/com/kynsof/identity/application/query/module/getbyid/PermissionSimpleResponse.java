package com.kynsof.identity.application.query.module.getbyid;

import com.kynsof.identity.domain.dto.PermissionDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PermissionSimpleResponse implements IResponse, Serializable {

    private UUID id;
    private String code;
    private String description;

    public PermissionSimpleResponse(PermissionDto response) {
        this.id = response.getId();
        this.code = response.getCode();
        this.description = response.getDescription();
    }

}
