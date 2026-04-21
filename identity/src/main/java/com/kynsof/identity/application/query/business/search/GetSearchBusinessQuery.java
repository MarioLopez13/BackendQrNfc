package com.kynsof.identity.application.query.business.search;

import com.kynsof.share.core.domain.bus.query.IQuery;
import com.kynsof.share.core.domain.request.FilterCriteria;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class GetSearchBusinessQuery implements IQuery {

    private final Pageable pageable;
    private final List<FilterCriteria> filter;
    private final String query;

    public GetSearchBusinessQuery(Pageable pageable, List<FilterCriteria> filter, String query) {
        this.pageable = pageable;
        this.filter = filter;
        this.query = query;
    }
}
