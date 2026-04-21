package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.business.geographiclocation.getall.GeographicLocationResponse;
import com.kynsof.identity.domain.dto.*;
import com.kynsof.identity.domain.dto.enumType.GeographicLocationType;
import com.kynsof.identity.domain.interfaces.service.IGeographicLocationService;
import com.kynsof.identity.infrastructure.entities.GeographicLocation;
import com.kynsof.identity.infrastructure.entities.projection.ProvinceCantonParishProjection;
import com.kynsof.identity.infrastructure.repository.command.GeographicLocationWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.GeographicLocationReadDataJPARepository;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import com.kynsof.share.core.infrastructure.redis.CacheConfig;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class GeographicLocationServiceImpl implements IGeographicLocationService {

    private final GeographicLocationReadDataJPARepository repositoryQuery;
    private final GeographicLocationWriteDataJPARepository repositoryCommand;

    public GeographicLocationServiceImpl(GeographicLocationReadDataJPARepository repositoryQuery,
                                         GeographicLocationWriteDataJPARepository repositoryCommand) {
        this.repositoryQuery = repositoryQuery;
        this.repositoryCommand = repositoryCommand;
    }

    @Override
    @Transactional
    public void create(GeographicLocationDto object) {
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(object, "GeographicLocation", "Geographic DTO cannot be null."));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(object.getId(), "GeographicLocation.id", "Geographic ID cannot be null."));
        repositoryCommand.save(new GeographicLocation(object));
    }

    @Override
    @Transactional
    public void delete(GeographicLocationDto delete) {
        var geolocation = new GeographicLocation(delete);
        geolocation.setName(delete.getName() + "-" + UUID.randomUUID());
        repositoryCommand.save(geolocation);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.LOCATION_CACHE, unless = "#result == null")
    public GeographicLocationDto findById(UUID id) {
        // Usar findByIdWithHierarchy para cargar toda la jerarquía con JOIN FETCH
        return repositoryQuery.findByIdWithHierarchy(id)
                .map(GeographicLocation::toAggregate)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.GEOGRAPHIC_LOCATION_NOT_FOUND, new ErrorField("id", "GeographicLocation not found."))));
    }

    @Override
    public PaginatedResponse findAll(Pageable pageable) {
        var data = repositoryQuery.findAll(pageable);
        return getPaginatedResponse(data);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.LOCATION_CACHE, unless = "#result == null")
    public LocationHierarchyDto findCantonAndProvinceIdsByParroquiaId(UUID parroquiaId) {
        // Usar findByIdWithHierarchy para cargar toda la jerarquía con JOIN FETCH
        var parroquia = repositoryQuery.findByIdWithHierarchy(parroquiaId)
                .filter(loc -> loc.getType() == GeographicLocationType.PARROQUIA && loc.getParent() != null)
                .orElseThrow(() -> new BusinessNotFoundException(new GlobalBusinessException(
                        DomainErrorMessage.GEOGRAPHIC_LOCATION_NOT_FOUND, new ErrorField("GeographicLocation.type", "Location not found."))));

        var canton = parroquia.getParent();
        if (canton.getType() != GeographicLocationType.CANTON || canton.getParent() == null) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.GEOGRAPHIC_LOCATION_NOT_FOUND, new ErrorField("GeographicLocation.type", "Location not found.")));
        }

        var province = canton.getParent();
        if (province.getType() != GeographicLocationType.PROVINCE) {
            throw new BusinessNotFoundException(new GlobalBusinessException(
                    DomainErrorMessage.GEOGRAPHIC_LOCATION_NOT_FOUND, new ErrorField("GeographicLocation.type", "Location not found.")));
        }

        return new LocationHierarchyDto(
                new ProvinceDto(province.getId(), province.getName()),
                new CantonDto(canton.getId(), canton.getName()),
                new ParroquiaDto(parroquia.getId(), parroquia.getName())
        );
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.LOCATION_CACHE, unless = "#result == null")
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {
        filterCriteria.forEach(filter -> {
            if ("type".equals(filter.getKey()) && filter.getValue() instanceof String) {
                try {
                    filter.setValue(GeographicLocationType.valueOf((String) filter.getValue()));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid value for GeographicLocationType enum: {}. Ignoring filter.", filter.getValue(), e);
                }
            }
        });

        var specifications = new GenericSpecificationsBuilder<GeographicLocation>(filterCriteria);
        var data = repositoryQuery.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<GeographicLocation> data) {
        // Usar toAggregateSimple() para evitar cargar jerarquía en listados (LAZY)
        // Para jerarquía completa, usar findById
        var responses = data.getContent().stream()
                .map(geographicLocation -> new GeographicLocationResponse(geographicLocation.toAggregateSimple()))
                .toList();
        return new PaginatedResponse(responses, data.getTotalPages(), data.getNumberOfElements(),
                data.getTotalElements(), data.getSize(), data.getNumber());
    }

    @Override
    @Cacheable(value = "search-locations", key = "#text != null ? #text : ''")
    public List<ProvinceDto> getAllProvincesWithCantonsAndParishes(String text) {
        List<ProvinceCantonParishProjection> rows = this.repositoryQuery.findProvincesWithCantonsAndParishes(text);

        // Map para agrupar (ProvinceId -> ProvinceDto)
        Map<UUID, ProvinceDto> provinceMap = new LinkedHashMap<>();

        for (ProvinceCantonParishProjection row : rows) {

            // --- Provincia ---
            UUID provId = row.getProvinceId();
            ProvinceDto provinceDto = provinceMap.get(provId);
            if (provinceDto == null) {
                provinceDto = new ProvinceDto(
                        provId,
                        row.getProvinceName(),
                        new ArrayList<>()  // lista vacía de cantones
                );
                provinceMap.put(provId, provinceDto);
            }

            // --- Cantón ---
            UUID cantonId = row.getCantonId();
            // Si no hay cantón (NULL), salta a la siguiente fila
            if (cantonId == null) {
                continue;
            }

            // Buscamos el cantón en la lista de cantones de la provincia actual
            List<CantonDto> cantones = provinceDto.getCantones();
            CantonDto cantonDto = cantones.stream()
                    .filter(c -> c.getId().equals(cantonId))
                    .findFirst()
                    .orElse(null);

            if (cantonDto == null) {
                // Aún no existe ese cantón en la lista de la provincia
                cantonDto = new CantonDto(
                        cantonId,
                        row.getCantonName(),
                        new ArrayList<>() // lista vacía de parroquias
                );
                cantones.add(cantonDto);
            }

            // --- Parroquia ---
            UUID parishId = row.getParishId();
            if (parishId == null) {
                continue;
            }

            // Evitar duplicados de parroquia
            boolean parishExists = cantonDto.getParroquias().stream()
                    .anyMatch(pa -> pa.getId().equals(parishId));

            if (!parishExists) {
                ParroquiaDto parroquiaDto = new ParroquiaDto(
                        parishId,
                        row.getParishName()
                );
                cantonDto.getParroquias().add(parroquiaDto);
            }
        }

        return new ArrayList<>(provinceMap.values());
    }

    @CacheEvict(value = "search-locations", allEntries = true)
    public void clearSearchLocationsCache() {
        // Este método no necesita lógica,
        // con la anotación @CacheEvict basta para limpiar
        // todos los entries de la caché "search-locations".
    }
}