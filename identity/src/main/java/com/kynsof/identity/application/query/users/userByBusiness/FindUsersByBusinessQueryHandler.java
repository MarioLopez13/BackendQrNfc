package com.kynsof.identity.application.query.users.userByBusiness;

import com.kynsof.identity.domain.interfaces.service.IUserSystemService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import org.springframework.stereotype.Component;

@Component
public class FindUsersByBusinessQueryHandler implements IQueryHandler<FindUsersByBusinessQuery, PaginatedResponse> {

    private final IUserSystemService userService;

    public FindUsersByBusinessQueryHandler(IUserSystemService userService) {
        this.userService = userService;
    }

    @Override
    public PaginatedResponse handle(FindUsersByBusinessQuery query) {
        PaginatedResponse users = userService.getUsersByBusiness(
                query.getBusinessId(),
                query.getEmail(),
                query.getName(),
                query.getLastName(),
                query.getPageable()
        );

        return users;
    }
}