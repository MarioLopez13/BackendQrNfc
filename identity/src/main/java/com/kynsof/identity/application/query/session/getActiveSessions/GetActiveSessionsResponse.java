package com.kynsof.identity.application.query.session.getActiveSessions;

import com.kynsof.identity.domain.dto.UserSessionDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetActiveSessionsResponse implements IResponse {
    private List<UserSessionDto> sessions;
    private int totalSessions;
}
