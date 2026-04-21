package com.kynsof.identity.application.query.geograficLocation;

import com.kynsof.share.core.domain.bus.query.IQuery;
import lombok.Getter;

@Getter
public class geograficLocationAllQuery implements IQuery {

  private final String query;

    public geograficLocationAllQuery(String query) {

        this.query = query;
    }

}
