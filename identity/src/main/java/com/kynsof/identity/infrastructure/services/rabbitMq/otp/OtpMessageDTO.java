package com.kynsof.identity.infrastructure.services.rabbitMq.otp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpMessageDTO implements Serializable {
    private String email;
    private String otpCode;
    private String name;
}