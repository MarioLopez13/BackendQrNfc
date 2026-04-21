package com.kynsof.identity.domain.interfaces.service;

import com.kynsof.identity.domain.dto.BusinessConfigurationDto;
import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de dominio para BusinessConfiguration.
 * Define las operaciones disponibles para gestionar configuraciones de negocio.
 */
public interface IBusinessConfigurationService {

    /**
     * Crea una nueva configuración de negocio
     */
    void create(BusinessConfigurationDto dto);

    /**
     * Actualiza una configuración existente
     */
    void update(BusinessConfigurationDto dto);

    /**
     * Elimina una configuración (soft delete)
     */
    void delete(BusinessConfigurationDto dto);

    /**
     * Busca una configuración por ID
     */
    BusinessConfigurationDto findById(UUID id);

    /**
     * Busca una configuración por businessId y configKey
     */
    BusinessConfigurationDto findByBusinessIdAndConfigKey(UUID businessId, String configKey);

    /**
     * Busca una configuración solo por configKey (sin filtrar por Business)
     */
    BusinessConfigurationDto findByConfigKey(String configKey);

    /**
     * Obtiene todas las configuraciones activas de un Business
     */
    List<BusinessConfigurationDto> findAllByBusinessId(UUID businessId);

    /**
     * Obtiene configuraciones de un Business filtradas por categoría
     */
    List<BusinessConfigurationDto> findByBusinessIdAndCategory(UUID businessId, EConfigCategory category);

    /**
     * Búsqueda paginada con filtros dinámicos
     */
    PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria);

    /**
     * Verifica si existe una configuración con la misma key para un Business (excluyendo un ID)
     */
    Long countByBusinessIdAndConfigKeyAndNotId(UUID businessId, String configKey, UUID id);

    /**
     * Verifica si existe una configuración con la misma key para un Business
     */
    Long countByBusinessIdAndConfigKey(UUID businessId, String configKey);
}
