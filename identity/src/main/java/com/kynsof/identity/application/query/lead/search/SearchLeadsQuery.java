package com.kynsof.identity.application.query.lead.search;

import com.kynsof.share.core.domain.bus.query.IQuery;
import com.kynsof.share.core.domain.request.FilterCriteria;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Getter
public class SearchLeadsQuery implements IQuery {

    private final Pageable pageable;
    private final List<FilterCriteria> filterCriteria;

    public SearchLeadsQuery(Pageable pageable, List<FilterCriteria> filterCriteria) {
        this.pageable = pageable;
        this.filterCriteria = filterCriteria;
    }
}
