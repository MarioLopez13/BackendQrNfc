package com.kynsof.share.core.domain.kafka.entity;

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
public class DoctorKafka implements Serializable {

    private UUID id;
    private String identification;
    private String code;
    private String email;
    private String name;
    private String lastName;
    private String image;
    private String business;
    private String registerNumber;
}