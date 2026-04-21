package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import com.kynsof.identity.infrastructure.entities.Lead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadReadDataJPARepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {

    Optional<Lead> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Lead> findByStatus(ELeadStatus status, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status = :status")
    Long countByStatus(@Param("status") ELeadStatus status);

    @Query("SELECT COUNT(l) FROM Lead l WHERE l.status NOT IN ('CONVERTED', 'DISCARDED')")
    Long countActiveLEads();
}
