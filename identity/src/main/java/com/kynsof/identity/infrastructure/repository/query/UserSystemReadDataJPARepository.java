package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.infrastructure.entities.UserSystem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserSystemReadDataJPARepository extends JpaRepository<UserSystem, UUID>, JpaSpecificationExecutor<UserSystem> {
    Page<UserSystem> findAll(Specification specification, Pageable pageable);

    @Query("SELECT COUNT(b) FROM UserSystem b WHERE b.userName = :userName AND b.id <> :id")
    Long countByUserNameAndNotId(@Param("userName") String userName, @Param("id") UUID id);

    @Query("SELECT COUNT(b) FROM UserSystem b WHERE b.email = :email AND b.id <> :id")
    Long countByEmailAndNotId(@Param("email") String email, @Param("id") UUID id);

    @Query("SELECT b FROM UserSystem b WHERE LOWER(b.email) = LOWER(:email)")
    Optional<UserSystem> findByEmail(@Param("email") String email);

    @Query("SELECT b FROM UserSystem b WHERE b.keyCloakId = :keyCloakId")
    Optional<UserSystem> findByKeyCloakId(UUID keyCloakId);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN TRUE ELSE FALSE END FROM UserSystem u WHERE LOWER(u.email) = LOWER(:email) AND u.status = :status")
    boolean existsByEmailAndStatus(@Param("email") String email, @Param("status") UserStatus status);

    @Query("SELECT DISTINCT u FROM UserSystem u " +
            "JOIN u.userPermissionBusinesses upb " +
            "WHERE upb.business.id = :businessId " +
            "AND (:email IS NULL OR :email = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
            "AND (:name IS NULL OR :name = '' OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:lastName IS NULL OR :lastName = '' OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%')))")
    Page<UserSystem> findUsersByBusinessAndFilters(
            @Param("businessId") UUID businessId,
            @Param("email") String email,
            @Param("name") String name,
            @Param("lastName") String lastName,
            Pageable pageable);
}
