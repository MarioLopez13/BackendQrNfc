package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.AdvertisementImage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdvertisementImageReadDataJPARepository extends JpaRepository<AdvertisementImage, UUID>,
        JpaSpecificationExecutor<AdvertisementImage> {

    Page<AdvertisementImage> findAll(Specification<AdvertisementImage> spec, Pageable pageable);

    List<AdvertisementImage> findByBusinessId(UUID businessId);
}
