package com.kynsof.identity.application.query.geograficLocation;

import com.kynsof.identity.domain.dto.ProvinceDto;
import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class GeograficLocationAllResponse implements IResponse {
    private List<ProvinceDto> results;
}