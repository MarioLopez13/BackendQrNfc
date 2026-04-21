package com.kynsof.identity.application.query.twofactor.status;

import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Get2FAStatusResponse implements IResponse {

    private boolean enabled;
    private boolean configured;
    private LocalDateTime enabledAt;
    private int remainingBackupCodes;

}
