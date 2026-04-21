package com.kynsof.identity.application.command.lead.capture;

import com.kynsof.identity.domain.dto.enumType.ELeadSource;
import com.kynsof.identity.domain.dto.enumType.ELeadType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CaptureLeadRequest {

    @NotNull(message = "El tipo de lead es requerido")
    private ELeadType type;

    private String businessName;

    @NotBlank(message = "El nombre de contacto es requerido")
    private String contactName;

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    private String phone;

    private String city;

    private String specialty;

    private Integer employeeCount;

    private String message;

    @NotNull(message = "La fuente del lead es requerida")
    private ELeadSource source;
}
