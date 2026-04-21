package com.kynsof.identity.domain.dto.mailjet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa una variable para ser utilizada en las plantillas de correo electrónico de Mailjet
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailJetVarDto {
    private String key;
    private Object value;
}
