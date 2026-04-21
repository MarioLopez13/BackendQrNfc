package com.kynsof.identity.domain.dto.mailjet;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Representa un archivo adjunto para correo electrónico en formato Base64
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MailJetAttachmentDto {
    private String contentType;
    private String fileName;
    private String base64Content;
}
