package com.kynsof.identity.application.query.session.getActiveSessions;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetActiveSessionsQuery implements IQuery {
    private String userId;
}
