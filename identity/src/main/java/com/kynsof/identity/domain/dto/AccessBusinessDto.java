package com.kynsof.identity.domain.dto;

import com.kynsof.identity.domain.dto.enumType.EOperationType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
public class AccessBusinessDto {
    protected UUID id;
    private UUID  businessId;
    private UUID createdBy;
    private EOperationType operationType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccessBusinessDto(UUID id, UUID businessId, UUID createdBy, EOperationType operationType ) {
        this.id = id;
        this.businessId = businessId;
        this.createdBy = createdBy;
        this.operationType = operationType;
    }
}
