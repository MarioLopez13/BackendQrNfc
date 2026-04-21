package com.kynsof.identity.domain.dto.mailjet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un destinatario de correo electrónico para Mailjet
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailJetRecipientDto {
    private String email;
    private String name;
}
