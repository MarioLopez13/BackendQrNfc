package com.kynsof.identity.application.query.users.existByEmail;

import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class ExistByEmailUserSystemsQueryHandler implements IQueryHandler<ExistByEmailUserSystemsQuery, UserSystemsExistByEmailResponse>  {

    private final IUserSystemService serviceImpl;

    public ExistByEmailUserSystemsQueryHandler(IUserSystemService serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    @Override
    public UserSystemsExistByEmailResponse handle(ExistByEmailUserSystemsQuery query) {
        boolean exist = serviceImpl.existsByEmailAndStatus(query.getEmail());

        return new UserSystemsExistByEmailResponse(exist);
    }
}
