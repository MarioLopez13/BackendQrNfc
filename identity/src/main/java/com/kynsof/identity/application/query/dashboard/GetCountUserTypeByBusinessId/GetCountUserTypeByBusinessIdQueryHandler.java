package com.kynsof.identity.application.query.dashboard.GetCountUserTypeByBusinessId;

import com.kynsof.identity.domain.interfaces.service.IUserPermissionBusinessService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetCountUserTypeByBusinessIdQueryHandler implements IQueryHandler<GetCountUserTypeByBusinessIdQuery, GetCountUserTypeByBusinessIdQueryResponse>  {

    private final IUserPermissionBusinessService serviceImpl;

    public GetCountUserTypeByBusinessIdQueryHandler(IUserPermissionBusinessService serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    @Override
    public GetCountUserTypeByBusinessIdQueryResponse handle(GetCountUserTypeByBusinessIdQuery query) {
        List<Map<String, Object>> resul = serviceImpl.countActiveUsersByTypeForBusiness(query.getBusinessId());

        return new GetCountUserTypeByBusinessIdQueryResponse(resul);
    }
}
