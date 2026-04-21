package com.kynsof.identity.domain.dto.mailjet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Objeto de solicitud para enviar correos electrónicos a través de Mailjet
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SendMailJetEmailRequestDto {
    private List<MailJetRecipientDto> recipientEmail;
    private List<MailJetVarDto> mailJetVars;
    private List<MailJetAttachmentDto> mailJetAttachments;
    private String subject;
    private String templateId;
}
