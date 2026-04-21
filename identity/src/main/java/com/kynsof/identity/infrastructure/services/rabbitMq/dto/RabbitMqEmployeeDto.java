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
public class RabbitMqEmployeeDto implements Serializable {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private UUID business;
}
