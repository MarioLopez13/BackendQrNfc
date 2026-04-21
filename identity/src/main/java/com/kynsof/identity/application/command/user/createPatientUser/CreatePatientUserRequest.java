package com.kynsof.identity.application.command.user.createPatientUser;

import com.kynsof.share.core.domain.EUserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePatientUserRequest {
    private String userName;
    private String email;
    private String name;
    private String lastName;
    private String password;
    private String image;
}
