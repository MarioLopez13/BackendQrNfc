package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.AccessBusiness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;



@Repository
public interface AccessBusinessReadDataJPARepository extends JpaRepository<AccessBusiness, UUID>, JpaSpecificationExecutor<AccessBusiness> {
    Page<AccessBusiness> findAll(Specification specification, Pageable pageable);
}
