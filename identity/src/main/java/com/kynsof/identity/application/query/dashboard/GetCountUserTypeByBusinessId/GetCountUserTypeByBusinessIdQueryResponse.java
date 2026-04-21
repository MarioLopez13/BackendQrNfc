package com.kynsof.identity.application.query.dashboard.GetCountUserTypeByBusinessId;

import com.kynsof.share.core.domain.bus.query.IResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class GetCountUserTypeByBusinessIdQueryResponse implements IResponse, Serializable {

    private List<Map<String, Object>> result;

}
