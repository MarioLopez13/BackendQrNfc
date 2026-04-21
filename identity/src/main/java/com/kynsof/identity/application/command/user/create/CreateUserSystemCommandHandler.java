package com.kynsof.identity.application.command.user.create;

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
import com.kynsof.share.core.domain.RulesChecker;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class CreateUserSystemCommandHandler implements ICommandHandler<CreateUserSystemCommand> {

    private final IUserSystemService userSystemService;
    private final IAuthService authService;
    private final ICloudBridgesFileService cloudBridgesService;

    @Autowired
    public CreateUserSystemCommandHandler(IUserSystemService userSystemService, IAuthService authService,
                                          ICloudBridgesFileService cloudBridgesService) {
        this.userSystemService = userSystemService;
        this.authService = authService;
        this.cloudBridgesService = cloudBridgesService;
    }

    @Override
    public void handle(CreateUserSystemCommand command) {
        RulesChecker.checkRule(new ModuleEmailMustBeUniqueRule(this.userSystemService, command.getEmail(), UUID.randomUUID()));
        RulesChecker.checkRule(new ModuleUserNameMustBeUniqueRule(this.userSystemService, command.getUserName(), UUID.randomUUID()));

        UserSystemKycloackRequest userSystemRequest = new UserSystemKycloackRequest(
                command.getUserName(),
                command.getEmail(),
                command.getName(),
                command.getLastName(),
                command.getPassword(),
                command.getUserType()
        );
        String userId = authService.registerUserSystem(userSystemRequest, true);

        UserSystemDto userDto = new UserSystemDto(
                command.getId(),
                command.getUserName(),
                command.getEmail(),
                command.getName(),
                command.getLastName(),
                UserStatus.ACTIVE,
                command.getImage()
        );
        userDto.setKeyCloakId(UUID.fromString(userId));
        userDto.setUserName(command.getUserName());
        userDto.setUserType(command.getUserType());

        UUID id = userSystemService.create(userDto);
        command.setId(id);
sendEmail(command);
    }

    private void sendEmail(CreateUserSystemCommand command) {
        try {
            // Crear el objeto de solicitud
            SendMailJetEmailRequestDto requestDto = new SendMailJetEmailRequestDto();

            // Configurar destinatario
            List<MailJetRecipientDto> recipients = new ArrayList<>();
            recipients.add(new MailJetRecipientDto(command.getEmail(), command.getLastName() + " " + command.getName()));
            requestDto.setRecipientEmail(recipients);
            LocalDate issueDate = LocalDate.now(); // o tu fecha específica


            // Configurar variables para la plantilla
            List<MailJetVarDto> vars = new ArrayList<>();
            vars.add(new MailJetVarDto("user_name", command.getEmail()));
            vars.add(new MailJetVarDto("temp_password", command.getPassword()));

            requestDto.setMailJetVars(vars);

            // Configurar el asunto
            requestDto.setSubject("Bienvenido a Kynsoft - Usuario creado");

            // ID de la plantilla en Mailjet (este es un ejemplo, debe configurarse el ID correcto)
            requestDto.setTemplateId("5965446");

            // Enviar la solicitud
            cloudBridgesService.sendEmail(requestDto);

        } catch (Exception e) {
            // Manejar el error de envío de correo
            throw new RuntimeException("Error al enviar el correo de bienvenida: " + e.getMessage());
        }
    }
}
