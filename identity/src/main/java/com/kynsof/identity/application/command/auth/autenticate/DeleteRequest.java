package com.kynsof.identity.application.command.auth.autenticate;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class DeleteRequest  {
   public String username;
   public String password;
}
