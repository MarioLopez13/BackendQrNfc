package com.kynsof.identity.infrastructure.services.rabbitMq.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusinessRabbitMQDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;
    private String name;
    private String latitude;
    private String longitude;
    private String logo;
    private String ruc;
    private String address;
    private String email;
    private String phone;
    private String zoneId;
}
