package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.identity.domain.dto.enumType.ELeadSource;
import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.identity.domain.dto.enumType.ELeadType;
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
@Table(
        name = "lead",
        schema = "identity",
        indexes = {
                @Index(name = "idx_lead_email", columnList = "email"),
                @Index(name = "idx_lead_status", columnList = "status"),
                @Index(name = "idx_lead_type", columnList = "type"),
                @Index(name = "idx_lead_source", columnList = "source"),
                @Index(name = "idx_lead_created_at", columnList = "created_at")
        }
)
public class Lead {

    @Id
    @Column(name = "id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ELeadType type;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String city;

    private String specialty;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ELeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ELeadStatus status;

    @Column(name = "converted_business_id")
    private UUID convertedBusinessId;

    @Column(name = "converted_user_id")
    private UUID convertedUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Lead(LeadDto dto) {
        this.id = dto.getId();
        this.type = dto.getType();
        this.businessName = dto.getBusinessName();
        this.contactName = dto.getContactName();
        this.email = dto.getEmail();
        this.phone = dto.getPhone();
        this.city = dto.getCity();
        this.specialty = dto.getSpecialty();
        this.employeeCount = dto.getEmployeeCount();
        this.message = dto.getMessage();
        this.source = dto.getSource();
        this.status = dto.getStatus();
        this.convertedBusinessId = dto.getConvertedBusinessId();
        this.convertedUserId = dto.getConvertedUserId();
        this.notes = dto.getNotes();
    }

    public LeadDto toAggregate() {
        return new LeadDto(
                id,
                type,
                businessName,
                contactName,
                email,
                phone,
                city,
                specialty,
                employeeCount,
                message,
                source,
                status,
                convertedBusinessId,
                convertedUserId,
                notes,
                createdAt,
                updatedAt
        );
    }
}