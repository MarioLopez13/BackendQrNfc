package com.kynsof.identity.infrastructure.services;

import com.kynsof.identity.application.query.accessbusiness.getById.AccessBusinessResponse;
import com.kynsof.identity.domain.dto.AccessBusinessDto;
import com.kynsof.identity.domain.interfaces.service.IAccessBusiessService;
import com.kynsof.identity.infrastructure.entities.AccessBusiness;
import com.kynsof.identity.infrastructure.repository.command.AccessBusinessWriteDataJPARepository;
import com.kynsof.identity.infrastructure.repository.query.AccessBusinessReadDataJPARepository;
import com.kynsof.share.core.domain.exception.BusinessNotFoundException;
import com.kynsof.share.core.domain.exception.DomainErrorMessage;
import com.kynsof.share.core.domain.exception.GlobalBusinessException;
import com.kynsof.share.core.domain.request.FilterCriteria;
import com.kynsof.share.core.domain.response.ErrorField;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.specifications.GenericSpecificationsBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class AccessBusinessServiceImpl implements IAccessBusiessService {

    private final AccessBusinessReadDataJPARepository query;
    private final AccessBusinessWriteDataJPARepository command;

    public AccessBusinessServiceImpl(AccessBusinessReadDataJPARepository query, AccessBusinessWriteDataJPARepository command) {
        this.query = query;
        this.command = command;
    }

    @Override
    public UUID create(AccessBusinessDto object) {
        return this.command.save(new AccessBusiness(object)).getId();
    }

    @Override
    public void update(AccessBusinessDto object) {
        this.command.save(new AccessBusiness(object));
    }

    @Override
    public void delete(UUID id) {
        try {
            this.command.deleteById(id);
        } catch (Exception e) {
            throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.NOT_DELETE,
                    new ErrorField("id", "Element cannot be deleted has a related element.")));
        }
    }

    @Override
    public AccessBusinessDto findById(UUID id) {
        Optional<AccessBusiness> object = this.query.findById(id);
        if (object.isPresent()) {
            return object.get().toAggregate();
        }
        throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.BUSINESS_NOT_FOUND,
                new ErrorField("id", "Access not found.")));
    }

    @Override
    public AccessBusinessDto getById(UUID id) {

        Optional<AccessBusiness> object = this.query.findById(id);
        if (object.isPresent()) {
            return object.get().toAggregate();
        }

        throw new BusinessNotFoundException(new GlobalBusinessException(DomainErrorMessage.BUSINESS_NOT_FOUND, new ErrorField("id", "Access not found.")));
    }

    @Override
    public PaginatedResponse search(Pageable pageable, List<FilterCriteria> filterCriteria) {

        GenericSpecificationsBuilder<AccessBusiness> specifications = new GenericSpecificationsBuilder<>(filterCriteria);
        Page<AccessBusiness> data = query.findAll(specifications, pageable);
        return getPaginatedResponse(data);
    }

    private PaginatedResponse getPaginatedResponse(Page<AccessBusiness> data) {
        List<AccessBusinessResponse> responses = new ArrayList<>();
        for (AccessBusiness entity : data.getContent()) {
            responses.add(new AccessBusinessResponse(entity.toAggregate()));
        }
        return new PaginatedResponse(
                responses,
                data.getTotalPages(),
                data.getNumberOfElements(),
                data.getTotalElements(),
                data.getSize(),
                data.getNumber()
        );
    }

    @Override
    public List<AccessBusinessDto> findAll() {
        return this.query.findAll().stream()
                .map(AccessBusiness::toAggregate)
                .collect(Collectors.toList());
    }
}
