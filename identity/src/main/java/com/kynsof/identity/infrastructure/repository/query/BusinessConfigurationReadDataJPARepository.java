package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.domain.dto.enumType.EConfigCategory;
import com.kynsof.identity.infrastructure.entities.BusinessConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de lectura para BusinessConfiguration.
 * Maneja operaciones de consulta y búsqueda.
 */
@Repository
public interface BusinessConfigurationReadDataJPARepository extends
        JpaRepository<BusinessConfiguration, UUID>,
        JpaSpecificationExecutor<BusinessConfiguration> {

    /**
     * Búsqueda paginada con especificaciones dinámicas
     */
    Page<BusinessConfiguration> findAll(Specification<BusinessConfiguration> specification, Pageable pageable);

    /**
     * Busca una configuración por businessId y configKey.
     * Método crítico para obtener configuraciones específicas de un negocio.
     */
    @Query("SELECT bc FROM BusinessConfiguration bc WHERE bc.business.id = :businessId AND bc.configKey = :configKey AND bc.isActive = true")
    Optional<BusinessConfiguration> findByBusinessIdAndConfigKey(
        @Param("businessId") UUID businessId,
        @Param("configKey") String configKey
    );

    /**
     * Obtiene todas las configuraciones activas de un Business específico
     */
    @Query("SELECT bc FROM BusinessConfiguration bc WHERE bc.business.id = :businessId AND bc.isActive = true ORDER BY bc.category, bc.configKey")
    List<BusinessConfiguration> findAllByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Obtiene configuraciones por Business y categoría
     */
    @Query("SELECT bc FROM BusinessConfiguration bc WHERE bc.business.id = :businessId AND bc.category = :category AND bc.isActive = true ORDER BY bc.configKey")
    List<BusinessConfiguration> findByBusinessIdAndCategory(
        @Param("businessId") UUID businessId,
        @Param("category") EConfigCategory category
    );

    /**
     * Verifica si existe una configuración con la misma key para un Business (excluyendo un ID específico)
     */
    @Query("SELECT COUNT(bc) FROM BusinessConfiguration bc WHERE bc.business.id = :businessId AND bc.configKey = :configKey AND bc.id <> :id")
    Long countByBusinessIdAndConfigKeyAndNotId(
        @Param("businessId") UUID businessId,
        @Param("configKey") String configKey,
        @Param("id") UUID id
    );

    /**
     * Verifica si existe una configuración con la misma key para un Business
     */
    @Query("SELECT COUNT(bc) FROM BusinessConfiguration bc WHERE bc.business.id = :businessId AND bc.configKey = :configKey")
    Long countByBusinessIdAndConfigKey(
        @Param("businessId") UUID businessId,
        @Param("configKey") String configKey
    );

    /**
     * Busca una configuración activa por configKey (sin filtrar por Business).
     * Útil cuando solo se conoce la clave de configuración.
     */
    @Query("SELECT bc FROM BusinessConfiguration bc WHERE bc.configKey = :configKey AND bc.isActive = true")
    Optional<BusinessConfiguration> findByConfigKey(@Param("configKey") String configKey);
}
