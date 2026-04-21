package com.kynsof.identity.application.command.auth.resendOtp;

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
public class ResendOtpCommandHandler implements ICommandHandler<ResendOtpCommand> {

    private final IRedisService redisService;
    private final ICloudBridgesFileService cloudBridgesService;
    private final IUserSystemService userSystemService;

    public ResendOtpCommandHandler(IRedisService redisService,
                                   ICloudBridgesFileService cloudBridgesService,
                                   IUserSystemService userSystemService) {
        this.redisService = redisService;
        this.cloudBridgesService = cloudBridgesService;
        this.userSystemService = userSystemService;
    }

    @Override
    public void handle(ResendOtpCommand command) {
        try {
            // Verificar que el usuario existe
            UserSystemDto userSystemDto = userSystemService.findByEmail(command.getEmail());
            if (userSystemDto == null) {
                throw new UserNotFoundException("User not found",
                    new ErrorField("email", "No user found with the provided email"));
            }

            // Eliminar el OTP anterior si existe
            try {
                redisService.deleteKey(command.getEmail());
            } catch (Exception e) {
                log.warn("No previous OTP found for email: {}", command.getEmail());
            }

            // Generar nuevo código OTP
            String otpCode = redisService.generateOtpCode();

            // Guardar el nuevo OTP en Redis
            redisService.saveOtpCode(command.getEmail(), otpCode);

            // Enviar el correo con el nuevo OTP
            sendOtpEmail(command.getEmail(), otpCode, userSystemDto.getName());

            // Establecer el mensaje de respuesta
            command.setMessage(new ResendOtpMessage("OTP code resent successfully"));

            log.info("OTP code regenerated and sent to email: {}", command.getEmail());

        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resending OTP to email: {}", command.getEmail(), e);
            throw new RuntimeException("Error generating and sending OTP code: " + e.getMessage());
        }
    }

    private void sendOtpEmail(String email, String otpCode, String userName) {
        try {
            // Crear el objeto de solicitud
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
            requestDto.setSubject("Código OTP de autenticación");

            // ID de la plantilla en Mailjet (mismo que se usa en AuthenticateCommandHandler)
            requestDto.setTemplateId("5964805");

            // Enviar la solicitud
            cloudBridgesService.sendEmail(requestDto);

            log.info("OTP email sent successfully to: {}", email);

        } catch (Exception e) {
            log.error("Error sending OTP email to: {}", email, e);
            throw new RuntimeException("Error sending OTP email: " + e.getMessage());
        }
    }
}
