package com.kynsof.identity.infrastructure.entities;


import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.identity.domain.dto.enumType.EOperationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "access_business", schema = "identity")
public class AccessBusiness {
    @Id
    @Column(name = "id")
    protected UUID id;


    @Column(name = "business_id")
    private UUID  businessId;
    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    private EOperationType operationType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    public AccessBusiness(AccessBusinessDto dto) {
        this.id = dto.getId();
        this.businessId = dto.getBusinessId();
        this.createdBy = dto.getCreatedBy();
        this.operationType = dto.getOperationType();
    }

    public AccessBusinessDto toAggregate(){
       AccessBusinessDto dto =   new AccessBusinessDto(id,businessId,createdBy,operationType);
       dto.setCreatedAt(createdAt);
       return dto;
    }
}
