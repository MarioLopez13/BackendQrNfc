package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.businessBalance.create.CreateBusinessBalanceCommand;
import com.kynsof.identity.application.command.businessBalance.create.CreateBusinessBalanceMessage;
import com.kynsof.identity.application.command.businessBalance.create.CreateBusinessBalanceRequest;
import com.kynsof.identity.application.command.businessBalance.discount.DiscountBusinessBalanceCommand;
import com.kynsof.identity.application.command.businessBalance.discount.DiscountBusinessBalanceMessage;
import com.kynsof.identity.application.command.businessBalance.discount.DiscountBusinessBalanceRequest;
import com.kynsof.identity.application.query.accessbusiness.search.GetSearchAccessBusinessQuery;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/business-balance")
public class BusinessBalanceController {
    private final IMediator mediator;
    private static final String USER_ID_HEADER = "X-User-ID";

    public BusinessBalanceController(IMediator mediator) {

        this.mediator = mediator;
    }

    @PostMapping()
    public ResponseEntity<?> addBalance(@RequestBody CreateBusinessBalanceRequest request,
                                        @RequestHeader(USER_ID_HEADER) String userId) {
        CreateBusinessBalanceCommand createCommand = CreateBusinessBalanceCommand.fromRequest(request, UUID.fromString(userId));
        CreateBusinessBalanceMessage response = mediator.send(createCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/discount")
    public ResponseEntity<?> discountBalance(@RequestBody DiscountBusinessBalanceRequest request,
                                             @RequestHeader(USER_ID_HEADER) String userId) {
        DiscountBusinessBalanceCommand createCommand = DiscountBusinessBalanceCommand.fromRequest(request,UUID.fromString(userId));
        DiscountBusinessBalanceMessage response = mediator.send(createCommand);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);

        GetSearchAccessBusinessQuery query = new GetSearchAccessBusinessQuery(pageable, request.getFilter(), request.getQuery());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }


   /*
   *  @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {

        FindAccessBusinessByIdQuery query = new FindAccessBusinessByIdQuery(id);
        AccessBusinessResponse response = mediator.send(query);

        return ResponseEntity.ok(response);
    }
   * */
}
