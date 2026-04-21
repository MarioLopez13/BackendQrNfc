package com.kynsof.identity.domain.dto;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para BusinessConfiguration.
 *
 * Representa una configuración clave-valor asociada a un Business,
 * permitiendo personalización multi-tenant flexible.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessConfigurationDto {

    /**
     * ID único de la configuración
     */
    private UUID id;

    /**
     * ID del Business al que pertenece esta configuración
     */
    private UUID businessId;

    /**
     * Clave única de configuración dentro del Business
     * Ejemplos: "primary_color", "whatsapp_api_key", "max_users"
     */
    private String configKey;

    /**
     * Valor de la configuración (puede estar encriptado)
     */
    private String configValue;

    /**
     * Categoría para agrupar configuraciones relacionadas
     */
    private EConfigCategory category;

    /**
     * Tipo de dato del valor para facilitar conversión
     */
    private EConfigDataType dataType;

    /**
     * Descripción legible de qué hace esta configuración
     */
    private String description;

    /**
     * Indica si el valor está encriptado
     */
    private Boolean isEncrypted;

    /**
     * Flag de activación (soft delete)
     */
    private Boolean isActive;

    /**
     * Timestamp de creación
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp de última actualización
     */
    private LocalDateTime updatedAt;

    /**
     * Constructor sin timestamps (para creación)
     */
    public BusinessConfigurationDto(UUID id, UUID businessId, String configKey,
                                    String configValue, EConfigCategory category,
                                    EConfigDataType dataType, String description,
                                    Boolean isEncrypted, Boolean isActive) {
        this.id = id;
        this.businessId = businessId;
        this.configKey = configKey;
        this.configValue = configValue;
        this.category = category;
        this.dataType = dataType;
        this.description = description;
        this.isEncrypted = isEncrypted != null ? isEncrypted : false;
        this.isActive = isActive != null ? isActive : true;
    }
}
