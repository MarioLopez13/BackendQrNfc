package com.kynsof.identity.application.command.auth.emailLogin.request;

import com.kynsof.identity.domain.dto.UserSystemDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetRecipientDto;
import com.kynsof.identity.domain.dto.mailjet.MailJetVarDto;
import com.kynsof.identity.domain.dto.mailjet.SendMailJetEmailRequestDto;
import com.kynsof.identity.domain.interfaces.service.ICloudBridgesFileService;
import com.kynsof.identity.domain.interfaces.service.IRedisService;
import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.command.ICommandHandler;
import com.kynsof.share.core.domain.exception.UserNotFoundException;
import com.kynsof.share.core.domain.response.ErrorField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EmailLoginRequestCommandHandler implements ICommandHandler<EmailLoginRequestCommand> {

    private final IRedisService redisService;
    private final ICloudBridgesFileService cloudBridgesService;
    private final IUserSystemService userSystemService;

    public EmailLoginRequestCommandHandler(IRedisService redisService,
                                           ICloudBridgesFileService cloudBridgesService,
                                           IUserSystemService userSystemService) {
        this.redisService = redisService;
        this.cloudBridgesService = cloudBridgesService;
        this.userSystemService = userSystemService;
    }

    @Override
    public void handle(EmailLoginRequestCommand command) {
        try {
            // 1. Verificar que el usuario existe en el sistema
            UserSystemDto userSystemDto = userSystemService.findByEmail(command.getEmail());
            if (userSystemDto == null) {
                throw new UserNotFoundException("Usuario no encontrado",
                    new ErrorField("email", "No existe un usuario con este correo electrónico"));
            }

            // 2. Eliminar OTP anterior si existe
            try {
                redisService.deleteKey(command.getEmail());
            } catch (Exception e) {
                log.debug("No previous OTP found for email: {}", command.getEmail());
            }

            // 3. Generar nuevo código OTP
            String otpCode = redisService.generateOtpCode();

            // 4. Guardar el OTP en Redis (expira en 20 minutos por defecto)
            redisService.saveOtpCode(command.getEmail(), otpCode);

            // 5. Enviar el correo con el código OTP
            sendOtpEmail(command.getEmail(), otpCode, userSystemDto.getName());

            // 6. Establecer resultado exitoso
            command.setResult(true);

            log.info("Email login OTP sent successfully to: {}", command.getEmail());

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing email login request for: {}", command.getEmail(), e);
            throw new RuntimeException("Error al procesar la solicitud de inicio de sesión: " + e.getMessage());
        }
    }

    private void sendOtpEmail(String email, String otpCode, String userName) {
        try {
            SendMailJetEmailRequestDto requestDto = new SendMailJetEmailRequestDto();

            // Configurar destinatario
            List<MailJetRecipientDto> recipients = new ArrayList<>();
            recipients.add(new MailJetRecipientDto(email, userName != null ? userName : "Usuario"));
            requestDto.setRecipientEmail(recipients);

            // Configurar variables para la plantilla
            List<MailJetVarDto> vars = new ArrayList<>();
            vars.add(new MailJetVarDto("otp_token", otpCode));
            requestDto.setMailJetVars(vars);

            // Configurar el asunto
            requestDto.setSubject("Código de acceso - Inicio de sesión");

            // ID de la plantilla en Mailjet
            requestDto.setTemplateId("5964805");

            // Enviar la solicitud
            cloudBridgesService.sendEmail(requestDto);

            log.info("Login OTP email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Error sending login OTP email to: {}", email, e);
            throw new RuntimeException("Error al enviar el correo con el código: " + e.getMessage());
        }
    }
}