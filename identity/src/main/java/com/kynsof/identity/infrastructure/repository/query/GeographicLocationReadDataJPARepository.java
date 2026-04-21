package com.kynsof.identity.infrastructure.repository.query;

import com.kynsof.identity.infrastructure.entities.GeographicLocation;
import com.kynsof.identity.infrastructure.entities.projection.ProvinceCantonParishProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GeographicLocationReadDataJPARepository extends JpaRepository<GeographicLocation, UUID>, JpaSpecificationExecutor<GeographicLocation> {
    Page<GeographicLocation> findAll(Specification specification, Pageable pageable);

    /**
     * Obtiene una ubicación con toda su jerarquía de padres (hasta 3 niveles).
     * Parroquia → Cantón → Provincia → País
     */
    @Query("SELECT g FROM GeographicLocation g " +
           "LEFT JOIN FETCH g.parent p " +
           "LEFT JOIN FETCH p.parent pp " +
           "LEFT JOIN FETCH pp.parent ppp " +
           "WHERE g.id = :id")
    Optional<GeographicLocation> findByIdWithHierarchy(@Param("id") UUID id);
    @Query(value = """
    SELECT 
        p.id   AS provinceId,
        p.name AS provinceName,
        p.type AS provinceType,

        c.id   AS cantonId,
        c.name AS cantonName,
        c.type AS cantonType,

        pa.id  AS parishId,
        pa.name AS parishName,
        pa.type AS parishType
    FROM geographiclocation p
    LEFT JOIN geographiclocation c 
           ON c.fk_pk_geographic_location = p.id
    LEFT JOIN geographiclocation pa
           ON pa.fk_pk_geographic_location = c.id
            WHERE p.type = 'PROVINCE'
  AND (
      COALESCE(:text, '') = ''\s
      OR p.name ILIKE '%' || :text || '%'
      OR c.name ILIKE '%' || :text || '%'
      OR pa.name ILIKE '%' || :text || '%'
  )
    """, nativeQuery = true)
    List<ProvinceCantonParishProjection> findProvincesWithCantonsAndParishes(@Param("text") String text);
}
