package com.kynsof.identity.application.command.business.update;

import com.kynsof.identity.domain.dto.BusinessDto;
import com.kynsof.identity.domain.dto.GeographicLocationDto;
import com.kynsof.identity.domain.interfaces.service.IBusinessService;
import com.kynsof.identity.domain.interfaces.service.IGeographicLocationService;
import com.kynsof.identity.domain.rules.business.BusinessNameMustBeUniqueRule;
import com.kynsof.identity.domain.rules.business.BusinessRucMustBeUniqueRule;
import com.kynsof.identity.infrastructure.services.rabbitMq.dto.BusinessRabbitMQDto;
import com.kynsof.identity.infrastructure.services.rabbitMq.eventPublisher.EventBusinessPublisherService;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.rules.ValidateObjectNotNullRule;
import com.kynsof.share.utils.UpdateIfNotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UpdateBusinessCommandHandler implements ICommandHandler<UpdateBusinessCommand> {
    
    private final IBusinessService service;
    private final IGeographicLocationService geographicLocationService;
    private final EventBusinessPublisherService eventPublisherService;
    
    public UpdateBusinessCommandHandler(IBusinessService service, IGeographicLocationService geographicLocationService, EventBusinessPublisherService eventPublisherService) {
        this.service = service;
        this.geographicLocationService = geographicLocationService;
        this.eventPublisherService = eventPublisherService;
    }
    
    @Override
    @Transactional
    public void handle(UpdateBusinessCommand command) {
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getGeographicLocation(), "geographicLocation", "GeographicLocation ID cannot be null."));
        RulesChecker.checkRule(new ValidateObjectNotNullRule<>(command.getId(), "id", "Business ID cannot be null."));
        
        GeographicLocationDto location = this.geographicLocationService.findById(command.getGeographicLocation());
        BusinessDto updateBusiness = this.service.getById(command.getId());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setRuc, command.getRuc());
        RulesChecker.checkRule(new BusinessRucMustBeUniqueRule(this.service, command.getRuc(), command.getId()));
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setDescription, command.getDescription());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setStatus, command.getStatus());
        
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setName, command.getName());
        RulesChecker.checkRule(new BusinessNameMustBeUniqueRule(this.service, command.getName(), command.getId()));
        
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setLongitude, command.getLongitude());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setLatitude, command.getLatitude());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setAddress, command.getAddress());

        updateBusiness.setGeographicLocationDto(location);

        UpdateIfNotNull.updateIfNotNull(updateBusiness::setLogo, command.getLogo());
        updateBusiness.setPhone(command.getPhone());
        updateBusiness.setEmail(command.getEmail());
        updateBusiness.setWebSite(command.getWebSite());
        updateBusiness.setStorageCapacity(command.getStorageCapacity());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setIdResponsible, command.getIdResponsible());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setFixedPrice, command.getFixedPrice());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setIsChargedPerConsultation, command.getIsChargedPerConsultation());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setSeller, command.getSeller());
        UpdateIfNotNull.updateIfNotNull(updateBusiness::setZoneId, command.getZoneId());
        service.update(updateBusiness);


        eventPublisherService.publishBusinessEvent(new BusinessRabbitMQDto(
                updateBusiness.getId(),
                updateBusiness.getName(),
                updateBusiness.getLatitude(),
                updateBusiness.getLongitude(),
                updateBusiness.getLogo(),
                updateBusiness.getRuc(),
                updateBusiness.getAddress(),
                updateBusiness.getEmail(),
                updateBusiness.getPhone(),
                updateBusiness.getZoneId()
        ));
    }
    
}
