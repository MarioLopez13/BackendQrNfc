package com.kynsof.identity.domain.dto;

import com.kynsof.identity.domain.dto.enumType.ELeadSource;
import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.identity.domain.dto.enumType.ELeadType;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadDto implements IResponse {

    private UUID id;
    private ELeadType type;
    private String businessName;
    private String contactName;
    private String email;
    private String phone;
    private String city;
    private String specialty;
    private Integer employeeCount;
    private String message;
    private ELeadSource source;
    private ELeadStatus status;
    private UUID convertedBusinessId;
    private UUID convertedUserId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LeadDto(UUID id, ELeadType type, String businessName, String contactName,
                   String email, String phone, String city, String specialty,
                   Integer employeeCount, String message, ELeadSource source) {
        this.id = id;
        this.type = type;
        this.businessName = businessName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.city = city;
        this.specialty = specialty;
        this.employeeCount = employeeCount;
        this.message = message;
        this.source = source;
        this.status = ELeadStatus.NEW;
    }
}