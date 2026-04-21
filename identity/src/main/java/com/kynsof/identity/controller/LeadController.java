package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.lead.capture.CaptureLeadCommand;
import com.kynsof.identity.application.command.lead.capture.CaptureLeadMessage;
import com.kynsof.identity.application.command.lead.capture.CaptureLeadRequest;
import com.kynsof.identity.application.command.lead.updatestatus.UpdateLeadStatusCommand;
import com.kynsof.identity.application.command.lead.updatestatus.UpdateLeadStatusMessage;
import com.kynsof.identity.application.command.lead.updatestatus.UpdateLeadStatusRequest;
import com.kynsof.identity.application.query.lead.getbyid.FindLeadByIdQuery;
import com.kynsof.identity.application.query.lead.search.SearchLeadsQuery;
import com.kynsof.identity.domain.dto.LeadDto;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import com.kynsof.share.core.domain.request.PageableUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "API para gestión de leads (clínicas y doctores interesados)")
public class LeadController {

    private final IMediator mediator;

    public LeadController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping("/capture")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Capturar un nuevo lead", description = "Endpoint público para capturar información de clínicas o doctores interesados")
    public ResponseEntity<CaptureLeadMessage> capture(@Valid @RequestBody CaptureLeadRequest request) {
        CaptureLeadCommand command = CaptureLeadCommand.fromRequest(request);
        return ResponseEntity.ok(mediator.send(command));
    }

    @PostMapping("/search")
    @Operation(summary = "Buscar leads", description = "Buscar leads con filtros y paginación")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        SearchLeadsQuery query = new SearchLeadsQuery(pageable, request.getFilter());
        return ResponseEntity.ok(mediator.send(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener lead por ID", description = "Obtener los detalles de un lead específico")
    public ResponseEntity<LeadDto> getById(@PathVariable UUID id) {
        FindLeadByIdQuery query = new FindLeadByIdQuery(id);
        return ResponseEntity.ok(mediator.send(query));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado del lead", description = "Cambiar el estado de un lead (CONTACTED, QUALIFIED, CONVERTED, etc.)")
    public ResponseEntity<UpdateLeadStatusMessage> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLeadStatusRequest request) {
        UpdateLeadStatusCommand command = new UpdateLeadStatusCommand(id, request);
        return ResponseEntity.ok(mediator.send(command));
    }
}
