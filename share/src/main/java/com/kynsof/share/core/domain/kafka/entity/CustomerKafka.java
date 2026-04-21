package com.kynsof.share.core.domain.kafka.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerKafka {

    private String id;
    private String identificationNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String image;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;
    private String profession;
}