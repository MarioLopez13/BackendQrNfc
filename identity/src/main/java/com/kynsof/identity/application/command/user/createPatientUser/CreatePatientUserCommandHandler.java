package com.kynsof.identity.application.command.user.createPatientUser;

import com.kynsof.identity.application.command.auth.registrySystemUser.UserSystemKycloackRequest;
import com.kynsof.identity.domain.dto.UserStatus;
import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetRecipientDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetVarDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.interfaces.service.IAuthService;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.identity.domain.rules.usersystem.ModuleEmailMustBeUniqueRule;
import com.kynsof.identity.domain.rules.usersystem.ModuleUserNameMustBeUniqueRule;
import com.kynsof.share.core.domain.EUserType;
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Handler para crear usuarios de tipo paciente.
 * A diferencia del handler general, este usa el keycloakId como ID del user_system
 * para que ambos valores sean iguales.
 */
@Component
@Slf4j
public class CreatePatientUserCommandHandler implements ICommandHandler<CreatePatientUserCommand> {

    private final IUserSystemService userSystemService;
    private final IAuthService authService;
    private final ICloudBridgesFileService cloudBridgesService;

    public CreatePatientUserCommandHandler(IUserSystemService userSystemService, IAuthService authService,
                                           ICloudBridgesFileService cloudBridgesService) {
        this.userSystemService = userSystemService;
        this.authService = authService;
        this.cloudBridgesService = cloudBridgesService;
    }

    @Override
    public void handle(CreatePatientUserCommand command) {
        RulesChecker.checkRule(new ModuleEmailMustBeUniqueRule(this.userSystemService, command.getEmail(), UUID.randomUUID()));
        RulesChecker.checkRule(new ModuleUserNameMustBeUniqueRule(this.userSystemService, command.getUserName(), UUID.randomUUID()));

        UserSystemKycloackRequest userSystemRequest = new UserSystemKycloackRequest(
                command.getUserName(),
                command.getEmail(),
                command.getName(),
                command.getLastName(),
                command.getPassword(),
                EUserType.PATIENTS
        );

        // Registrar usuario en Keycloak y obtener el keycloakId
        String keycloakIdStr = authService.registerUserSystem(userSystemRequest, true);
        UUID keycloakId = UUID.fromString(keycloakIdStr);

        // Usar keycloakId como ID del user_system para que ambos sean iguales
        UserSystemDto userDto = new UserSystemDto(
                keycloakId,
                command.getUserName(),
                command.getEmail(),
                command.getName(),
                command.getLastName(),
                UserStatus.ACTIVE,
                command.getImage()
        );
        userDto.setKeyCloakId(keycloakId);
        userDto.setUserName(command.getUserName());
        userDto.setUserType(EUserType.PATIENTS);

        UUID id = userSystemService.create(userDto);
        command.setId(id);

        sendEmail(command);
    }

    private void sendEmail(CreatePatientUserCommand command) {
        try {
            SendMailJetEmailRequestDto requestDto = new SendMailJetEmailRequestDto();

            List<MailJetRecipientDto> recipients = new ArrayList<>();
            recipients.add(new MailJetRecipientDto(command.getEmail(), command.getLastName() + " " + command.getName()));
            requestDto.setRecipientEmail(recipients);

            List<MailJetVarDto> vars = new ArrayList<>();
            vars.add(new MailJetVarDto("user_name", command.getEmail()));
            vars.add(new MailJetVarDto("temp_password", command.getPassword()));
            requestDto.setMailJetVars(vars);

            requestDto.setSubject("Bienvenido a Kynsoft - Usuario creado");
            requestDto.setTemplateId("5965446");

            cloudBridgesService.sendEmail(requestDto);

        } catch (Exception e) {
            log.error("Error al enviar el correo de bienvenida: {}", e.getMessage());
            // No lanzar excepcion para no afectar el flujo principal
        }
    }
}
