package com.kynsof.identity.application.query.profile.getbyid;

import com.kynsof.identity.application.query.profile.search.ProfileSearchResponse;
import com.kynsof.identity.domain.dto.ProfileDto;
import com.kynsof.identity.domain.interfaces.service.IProfileService;
import com.kynsof.share.core.domain.bus.query.IQueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GetProfileByIdQueryHandler implements IQueryHandler<GetProfileByIdQuery, ProfileSearchResponse> {

    private final IProfileService profileService;

    public GetProfileByIdQueryHandler(IProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    public ProfileSearchResponse handle(GetProfileByIdQuery query) {
        ProfileDto profileDto = profileService.findById(query.getId());
        return new ProfileSearchResponse(profileDto);
    }
}
