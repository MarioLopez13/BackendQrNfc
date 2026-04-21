package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.Permission;
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

public interface PermissionReadDataJPARepository extends JpaRepository<Permission, UUID>, JpaSpecificationExecutor<Permission> {

    /**
     * Búsqueda con Specification + módulo cargado via EntityGraph.
     * Evita LazyInitializationException al acceder a permission.getModule().
     */
    @Override
    @EntityGraph(attributePaths = {"module"})
    Page<Permission> findAll(Specification<Permission> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"module"})
    Page<Permission> findAll(Pageable pageable);

    @Query("SELECT p FROM Permission p WHERE p.module.id = :moduleId")
    Set<Permission> findByModuleId(UUID moduleId);

    @Query("SELECT p FROM Permission p JOIN p.userPermissionBusinesses urb JOIN urb.business b " +
            "WHERE p.module.id = :moduleId AND b.id = :businessId")
    List<Permission> findByModuleIdAndBusinessId(@Param("moduleId") UUID moduleId, @Param("businessId") UUID businessId);

    @Query("SELECT COUNT(b) FROM Permission b WHERE b.code = :code AND b.id <> :id")
    Long countByCodeAndNotId(@Param("code") String code, @Param("id") UUID id);

    /**
     * Obtiene un permiso con su módulo cargado (JOIN FETCH).
     * Usar cuando se necesite acceder a permission.getModule().
     */
    @Query("SELECT p FROM Permission p LEFT JOIN FETCH p.module WHERE p.id = :id")
    Optional<Permission> findByIdWithModule(@Param("id") UUID id);

    /**
     * Búsqueda paginada de permisos con módulo cargado.
     * Nota: No se puede usar Specification con JOIN FETCH, usar para casos específicos.
     */
    @Query(value = "SELECT p FROM Permission p LEFT JOIN FETCH p.module",
           countQuery = "SELECT COUNT(p) FROM Permission p")
    Page<Permission> findAllWithModule(Pageable pageable);

}
