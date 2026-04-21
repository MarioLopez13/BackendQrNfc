package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.Permission;
import com.kynsof.identity.infrastructure.entities.UserPermissionBusiness;
import com.kynsof.identity.infrastructure.repository.query.projections.BusinessPermissionProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface UserPermissionBusinessReadDataJPARepository extends JpaRepository<UserPermissionBusiness, UUID>, JpaSpecificationExecutor<UserPermissionBusiness> {

    /**
     * Búsqueda con Specification + relaciones cargadas via EntityGraph.
     * Evita LazyInitializationException al acceder a user, permission, business.
     */
    @Override
    @EntityGraph(attributePaths = {"user", "permission", "business"})
    Page<UserPermissionBusiness> findAll(Specification specification, Pageable pageable);

    /**
     * Obtiene un UserPermissionBusiness con todas sus relaciones cargadas (JOIN FETCH).
     */
    @Query("SELECT upb FROM UserPermissionBusiness upb " +
           "LEFT JOIN FETCH upb.user " +
           "LEFT JOIN FETCH upb.permission " +
           "LEFT JOIN FETCH upb.business " +
           "WHERE upb.id = :id")
    Optional<UserPermissionBusiness> findByIdWithRelations(@Param("id") UUID id);

    @Query("SELECT COUNT(upb) FROM UserPermissionBusiness upb WHERE upb.user.id = :userId AND upb.business.id = :businessId")
    Long countByUserAndBusiness(UUID userId, UUID businessId);

    @Query("SELECT p FROM UserPermissionBusiness upb JOIN upb.permission p LEFT JOIN FETCH p.module WHERE upb.user.id = :userId AND upb.business.id = :businessId")
    Set<Permission> findPermissionsByUserIdAndBusinessId(@Param("userId") UUID userId, @Param("businessId") UUID businessId);

    @EntityGraph(attributePaths = {"user", "permission", "business"})
    List<UserPermissionBusiness> findUserPermissionBusinessByUserId(UUID userId);

    @Query("SELECT upb.user.userType, COUNT(DISTINCT upb.user.id) " +
            "FROM UserPermissionBusiness upb " +
            "WHERE upb.business.id = :businessId " +
            "AND upb.user.status = 'ACTIVE' " + // Solo usuarios activos
            "GROUP BY upb.user.userType " +
            "ORDER BY upb.user.userType")
    List<Object[]> countActiveUsersByTypeForBusiness(@Param("businessId") UUID businessId);

    /**
     * Query optimizada para UserMe: obtiene negocios y permisos en una sola consulta.
     * Usa proyección para traer solo los campos necesarios, evitando cargar entidades completas.
     * Resultado: una fila por cada combinación business-permission del usuario.
     */
    @Query("""
            SELECT
                b.id as businessId,
                b.name as businessName,
                b.balance as balance,
                b.idResponsible as idResponsible,
                p.code as permissionCode
            FROM UserPermissionBusiness upb
            JOIN upb.business b
            JOIN upb.permission p
            WHERE upb.user.id = :userId
            ORDER BY b.name
            """)
    List<BusinessPermissionProjection> findBusinessPermissionsOptimized(@Param("userId") UUID userId);

    /**
     * Query optimizada para UserMe con filtro por businessId.
     * Solo trae los permisos de una empresa específica.
     */
    @Query("""
            SELECT
                b.id as businessId,
                b.name as businessName,
                b.balance as balance,
                b.idResponsible as idResponsible,
                p.code as permissionCode
            FROM UserPermissionBusiness upb
            JOIN upb.business b
            JOIN upb.permission p
            WHERE upb.user.id = :userId AND b.id = :businessId
            ORDER BY b.name
            """)
    List<BusinessPermissionProjection> findBusinessPermissionsOptimizedByBusiness(
            @Param("userId") UUID userId,
            @Param("businessId") UUID businessId);

}
