package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProfileReadDataJPARepository extends JpaRepository<Profile, UUID>, JpaSpecificationExecutor<Profile> {

    @Override
    @EntityGraph(attributePaths = {"permissions", "permissions.module"})
    Page<Profile> findAll(Specification<Profile> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"permissions", "permissions.module"})
    Page<Profile> findAll(Pageable pageable);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions perm LEFT JOIN FETCH perm.module WHERE p.id = :id")
    Optional<Profile> findByIdWithPermissions(@Param("id") UUID id);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.name = :name AND p.id <> :id")
    Long countByNameAndNotId(@Param("name") String name, @Param("id") UUID id);

    @Query("SELECT COUNT(p) FROM Profile p WHERE p.code = :code AND p.id <> :id")
    Long countByCodeAndNotId(@Param("code") String code, @Param("id") UUID id);

    Optional<Profile> findByName(String name);

    Optional<Profile> findByCode(String code);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions perm LEFT JOIN FETCH perm.module WHERE p.name = :name")
    Optional<Profile> findByNameWithPermissions(@Param("name") String name);

    @Query("SELECT p FROM Profile p LEFT JOIN FETCH p.permissions perm LEFT JOIN FETCH perm.module WHERE p.code = :code")
    Optional<Profile> findByCodeWithPermissions(@Param("code") String code);
}
