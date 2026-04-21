package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.BusinessModuleDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "business_module", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
public class BusinessModule {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private ModuleSystem module;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;


    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public BusinessModule(BusinessModuleDto businessModuleDto) {
        this.id = businessModuleDto.getId();
        this.business = new Business(businessModuleDto.getBusiness());
        this.module = new ModuleSystem(businessModuleDto.getModule());
    }

    public BusinessModuleDto toAggregate () {
        // Usar toAggregateSimple() para módulo para evitar cargar permissions (LAZY)
        return new BusinessModuleDto(
                id,
                business.toAggregate(),
                module.toAggregateSimple()
        );
    }

}
