package com.kynsof.identity.application.query.users.getSearch;

import com.kynsof.share.core.domain.bus.query.IQuery;
import com.kynsof.share.core.domain.request.FilterCriteria;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class GetSearchUserSystemsQuery implements IQuery {

    private final Pageable pageable;
    private final List<FilterCriteria> filter;
    private final String query;

    public GetSearchUserSystemsQuery(Pageable pageable, List<FilterCriteria> filter, String query) {
        this.pageable = pageable;
        this.filter = filter;
        this.query = query;
    }
}
