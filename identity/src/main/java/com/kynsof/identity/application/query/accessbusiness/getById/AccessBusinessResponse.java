package com.kynsof.identity.application.query.accessbusiness.getById;

import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.identity.domain.dto.enumType.EOperationType;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@AllArgsConstructor
@Getter
@Setter
public class AccessBusinessResponse implements IResponse, Serializable {
    protected UUID id;
    private UUID  businessId;
    private UUID createdBy;
    private EOperationType operationType;
    private LocalDateTime createdAt;

    public AccessBusinessResponse(AccessBusinessDto dto) {
        this.id = dto.getId();
        this.businessId = dto.getBusinessId();
        this.createdBy = dto.getCreatedBy();
        this.operationType = dto.getOperationType();
        this.createdAt = dto.getCreatedAt();
    }
}
