package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.BusinessModule;
import com.kynsof.identity.infrastructure.entities.ModuleSystem;
import com.kynsof.identity.infrastructure.repository.query.projections.BusinessPermissionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessModuleReadDataJPARepository extends JpaRepository<BusinessModule, UUID>, JpaSpecificationExecutor<BusinessModule> {

    /**
     * Búsqueda con Specification + relaciones cargadas via EntityGraph.
     * Evita LazyInitializationException al acceder a business, module, business.geographicLocation.
     */
    @Override
    @EntityGraph(attributePaths = {"business", "business.geographicLocation", "module"})
    Page<BusinessModule> findAll(Specification specification, Pageable pageable);

    /**
     * Obtiene un BusinessModule con todas sus relaciones cargadas (JOIN FETCH).
     */
    @Query("SELECT bm FROM BusinessModule bm " +
           "LEFT JOIN FETCH bm.business b " +
           "LEFT JOIN FETCH b.geographicLocation " +
           "LEFT JOIN FETCH bm.module " +
           "WHERE bm.id = :id")
    Optional<BusinessModule> findByIdWithRelations(@Param("id") UUID id);

    /**
     * Obtiene BusinessModules con business y module cargados (JOIN FETCH).
     */
    @Query("SELECT bm FROM BusinessModule bm " +
           "LEFT JOIN FETCH bm.business b " +
           "LEFT JOIN FETCH b.geographicLocation " +
           "LEFT JOIN FETCH bm.module " +
           "WHERE bm.business.id = :businessId")
    List<BusinessModule> findByBusinessId(@Param("businessId") UUID businessId);

    @Query("SELECT bm.module FROM BusinessModule bm WHERE bm.business.id = :businessId")
    List<ModuleSystem> findModulesByBusinessId(@Param("businessId") UUID businessId);

    /**
     * Obtiene módulos con permisos cargados (JOIN FETCH) para un business.
     * Usar para buildStructure y endpoints que necesiten los permisos.
     */
    @Query("SELECT DISTINCT m FROM BusinessModule bm JOIN bm.module m LEFT JOIN FETCH m.permissions WHERE bm.business.id = :businessId")
    List<ModuleSystem> findModulesWithPermissionsByBusinessId(@Param("businessId") UUID businessId);

    @Query("SELECT m FROM BusinessModule bm JOIN bm.module m WHERE bm.business.id = :businessId")
    List<ModuleSystem> findModuleSystemByBusinessId(UUID businessId);

    @Query("SELECT COUNT(b) FROM BusinessModule b WHERE b.business.id = :businessId AND b.module.id = :moduleId")
    Long countByBussinessIdAndModuleId(@Param("businessId") UUID businessId, @Param("moduleId") UUID moduleId);

    @Query("SELECT bm.business.id, bm.business.name, p.code, bm.business.balance, bm.business.idResponsible FROM BusinessModule bm JOIN bm.module.permissions p")
    List<Object[]> findAllBusinessesWithPermissions();

    /**
     * Query optimizada para SUPER_ADMIN: obtiene todos los negocios activos con permisos.
     * Filtra solo negocios con status ACTIVE para mejorar rendimiento.
     * Usa proyección para traer solo los campos necesarios.
     */
    @Query("""
            SELECT
                b.id as businessId,
                b.name as businessName,
                b.balance as balance,
                b.idResponsible as idResponsible,
                p.code as permissionCode
            FROM BusinessModule bm
            JOIN bm.business b
            JOIN bm.module m
            JOIN m.permissions p
            WHERE b.status = 'ACTIVE'
            ORDER BY b.name
            """)
    List<BusinessPermissionProjection> findAllBusinessesWithPermissionsOptimized();

    /**
     * Query optimizada para SUPER_ADMIN con filtro por businessId.
     * Solo trae los permisos de una empresa específica.
     */
    @Query("""
            SELECT
                b.id as businessId,
                b.name as businessName,
                b.balance as balance,
                b.idResponsible as idResponsible,
                p.code as permissionCode
            FROM BusinessModule bm
            JOIN bm.business b
            JOIN bm.module m
            JOIN m.permissions p
            WHERE b.status = 'ACTIVE' AND b.id = :businessId
            ORDER BY b.name
            """)
    List<BusinessPermissionProjection> findBusinessWithPermissionsOptimizedById(@Param("businessId") UUID businessId);
}
