package com.kynsof.identity.application.query.users.existByEmail;

import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserSystemsExistByEmailResponse implements IResponse {
    private boolean isExist;
}