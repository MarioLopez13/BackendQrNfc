package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.businessConfiguration.create.CreateBusinessConfigurationCommand;
import com.kynsof.identity.application.command.businessConfiguration.create.CreateBusinessConfigurationMessage;
import com.kynsof.identity.application.command.businessConfiguration.create.CreateBusinessConfigurationRequest;
import com.kynsof.identity.application.command.businessConfiguration.delete.DeleteBusinessConfigurationCommand;
import com.kynsof.identity.application.command.businessConfiguration.delete.DeleteBusinessConfigurationMessage;
import com.kynsof.identity.application.command.businessConfiguration.update.UpdateBusinessConfigurationCommand;
import com.kynsof.identity.application.command.businessConfiguration.update.UpdateBusinessConfigurationMessage;
import com.kynsof.identity.application.command.businessConfiguration.update.UpdateBusinessConfigurationRequest;
import com.kynsof.identity.application.query.businessConfiguration.getByBusinessAndKey.FindBusinessConfigurationByBusinessAndKeyQuery;
import com.kynsof.identity.application.query.businessConfiguration.getById.BusinessConfigurationResponse;
import com.kynsof.identity.application.query.businessConfiguration.getById.FindBusinessConfigurationByIdQuery;
import com.kynsof.identity.application.query.businessConfiguration.getByKey.FindBusinessConfigurationByKeyQuery;
import com.kynsof.identity.application.query.businessConfiguration.search.SearchBusinessConfigurationQuery;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST para BusinessConfiguration.
 * Expone endpoints para gestionar configuraciones de negocio.
 */
@RestController
@RequestMapping("/api/business-configuration")
public class BusinessConfigurationController {

    private final IMediator mediator;

    public BusinessConfigurationController(IMediator mediator) {
        this.mediator = mediator;
    }

    /**
     * POST /api/business-configuration
     * Crea una nueva configuración de negocio
     */
    @PostMapping
    public ResponseEntity<CreateBusinessConfigurationMessage> create(@RequestBody CreateBusinessConfigurationRequest request) {
        CreateBusinessConfigurationCommand command = CreateBusinessConfigurationCommand.fromRequest(request);
        CreateBusinessConfigurationMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/business-configuration/{id}
     * Busca una configuración por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusinessConfigurationResponse> getById(@PathVariable UUID id) {
        FindBusinessConfigurationByIdQuery query = new FindBusinessConfigurationByIdQuery(id);
        BusinessConfigurationResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/business-configuration/key/{configKey}
     * Busca una configuración solo por configKey
     */
    @GetMapping("/key/{configKey}")
    public ResponseEntity<BusinessConfigurationResponse> getByKey(@PathVariable String configKey) {
        FindBusinessConfigurationByKeyQuery query = new FindBusinessConfigurationByKeyQuery(configKey);
        BusinessConfigurationResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/business-configuration/business/{businessId}/key/{configKey}
     * Busca una configuración por businessId y configKey
     */
    @GetMapping("/business/{businessId}/key/{configKey}")
    public ResponseEntity<BusinessConfigurationResponse> getByBusinessAndKey(
            @PathVariable UUID businessId,
            @PathVariable String configKey) {
        FindBusinessConfigurationByBusinessAndKeyQuery query =
                new FindBusinessConfigurationByBusinessAndKeyQuery(businessId, configKey);
        BusinessConfigurationResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/business-configuration/search
     * Búsqueda paginada con filtros dinámicos
     */
    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        SearchBusinessConfigurationQuery query = new SearchBusinessConfigurationQuery(
                pageable,
                request.getFilter(),
                request.getQuery()
        );
        PaginatedResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/business-configuration/{id}
     * Actualiza una configuración existente
     */
    @PatchMapping("/{id}")
    public ResponseEntity<UpdateBusinessConfigurationMessage> update(
            @PathVariable UUID id,
            @RequestBody UpdateBusinessConfigurationRequest request) {
        UpdateBusinessConfigurationCommand command = UpdateBusinessConfigurationCommand.fromRequest(request, id);
        UpdateBusinessConfigurationMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    /**
     * DELETE /api/business-configuration/{id}
     * Elimina una configuración (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteBusinessConfigurationMessage> delete(@PathVariable UUID id) {
        DeleteBusinessConfigurationCommand command = new DeleteBusinessConfigurationCommand(id);
        DeleteBusinessConfigurationMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }
}
