package com.kynsof.identity.controller;

import com.kynsof.identity.application.query.dashboard.GetCountUserTypeByBusinessId.GetCountUserTypeByBusinessIdQuery;
import com.kynsof.identity.application.query.dashboard.GetCountUserTypeByBusinessId.GetCountUserTypeByBusinessIdQueryResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final IMediator mediator;

    public DashboardController(IMediator mediator) {

        this.mediator = mediator;
    }

    @GetMapping(path = "/user-count-by-type/{business}")
    public ResponseEntity<?> getUserCountByType(@PathVariable UUID business) {

        GetCountUserTypeByBusinessIdQuery query = new GetCountUserTypeByBusinessIdQuery(business);
        GetCountUserTypeByBusinessIdQueryResponse response = mediator.send(query);

        return ResponseEntity.ok(response);
    }


}
