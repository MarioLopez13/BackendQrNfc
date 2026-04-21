package com.kynsof.identity.application.command.lead.updatestatus;

import com.kynsof.identity.domain.dto.enumType.ELeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLeadStatusRequest {

    @NotNull(message = "El estado es requerido")
    private ELeadStatus status;

    private String notes;
}
