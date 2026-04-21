package com.kynsof.identity.infrastructure.entities;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.domain.dto.enumType.EConfigDataType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad que representa una configuración específica de un Business.
 *
 * Permite almacenar pares clave-valor de configuración por empresa,
 * facilitando la personalización multi-tenant sin modificar código.
 *
 * Características:
 * - Cada Business puede tener múltiples configuraciones
 * - Las claves son únicas por Business (constraint UK)
 * - Soporta encriptación para valores sensibles
 * - Permite categorización para facilitar búsquedas
 * - Soft delete mediante isActive flag
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(
    name = "business_configuration",
    schema = "identity",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_business_config_key",
        columnNames = {"business_id", "config_key"}
    ),
    indexes = {
        @Index(name = "idx_business_config_business_id", columnList = "business_id"),
        @Index(name = "idx_business_config_category", columnList = "business_id, category, is_active")
    }
)
public class BusinessConfiguration {

    @Id
    @Column(name = "id")
    private UUID id;

    /**
     * Relación Many-to-One con Business.
     * Cada configuración pertenece a un único Business.
     * Lazy loading para optimizar performance.
     */
    @Column(name = "business_id", insertable = false, updatable = false)
    private UUID businessId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    /**
     * Clave única de configuración dentro del Business.
     * Ejemplos: "whatsapp_api_token", "max_concurrent_rides"
     */
    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    /**
     * Valor de la configuración.
     * Puede contener strings, números, booleans, JSON, etc.
     * Si isEncrypted es true, este valor está encriptado.
     */
    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    /**
     * Categoría de la configuración para facilitar agrupación y filtrado.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private EConfigCategory category;

    /**
     * Tipo de dato del valor para facilitar conversión en cliente.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private EConfigDataType dataType;

    /**
     * Descripción legible de la configuración.
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Indica si el valor está encriptado.
     * Valores sensibles (API keys, tokens, passwords) deben estar encriptados.
     */
    @Column(name = "is_encrypted")
    private Boolean isEncrypted = false;

    /**
     * Flag de activación (soft delete).
     * Permite desactivar configuraciones sin eliminarlas.
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Timestamp de creación (automático)
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp de última actualización (automático)
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Constructor desde DTO
     */
    public BusinessConfiguration(BusinessConfigurationDto dto) {
        this.id = dto.getId();
        this.configKey = dto.getConfigKey();
        this.configValue = dto.getConfigValue();
        this.category = dto.getCategory();
        this.dataType = dto.getDataType();
        this.description = dto.getDescription();
        this.isEncrypted = dto.getIsEncrypted();
        this.isActive = dto.getIsActive();
    }

    /**
     * Convierte la entidad a DTO
     */
    public BusinessConfigurationDto toAggregate() {
        BusinessConfigurationDto dto = new BusinessConfigurationDto(
            id,
            business != null ? business.getId() : null,
            configKey,
            configValue,
            category,
            dataType,
            description,
            isEncrypted,
            isActive
        );
        dto.setCreatedAt(createdAt);
        dto.setUpdatedAt(updatedAt);
        return dto;
    }

    /**
     * Actualiza los valores desde un DTO
     */
    public void update(BusinessConfigurationDto dto) {
        if (dto.getConfigValue() != null) {
            this.configValue = dto.getConfigValue();
        }
        if (dto.getCategory() != null) {
            this.category = dto.getCategory();
        }
        if (dto.getDataType() != null) {
            this.dataType = dto.getDataType();
        }
        if (dto.getDescription() != null) {
            this.description = dto.getDescription();
        }
        if (dto.getIsEncrypted() != null) {
            this.isEncrypted = dto.getIsEncrypted();
        }
        if (dto.getIsActive() != null) {
            this.isActive = dto.getIsActive();
        }
    }

    /**
     * Activa la configuración
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Desactiva la configuración (soft delete)
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Toggle del estado activo/inactivo
     */
    public void toggleActive() {
        this.isActive = !this.isActive;
    }
}
