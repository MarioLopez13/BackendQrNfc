package com.kynsof.identity.application.query.businessConfiguration.getById;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO para BusinessConfiguration
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessConfigurationResponse implements IResponse {

    private UUID id;
    private UUID businessId;
    private String configKey;
    private String configValue;
    private EConfigCategory category;
    private EConfigDataType dataType;
    private String description;
    private Boolean isEncrypted;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BusinessConfigurationResponse(BusinessConfigurationDto dto) {
        this.id = dto.getId();
        this.businessId = dto.getBusinessId();
        this.configKey = dto.getConfigKey();
        this.configValue = dto.getConfigValue();
        this.category = dto.getCategory();
        this.dataType = dto.getDataType();
        this.description = dto.getDescription();
        this.isEncrypted = dto.getIsEncrypted();
        this.isActive = dto.getIsActive();
        this.createdAt = dto.getCreatedAt();
        this.updatedAt = dto.getUpdatedAt();
    }
}
