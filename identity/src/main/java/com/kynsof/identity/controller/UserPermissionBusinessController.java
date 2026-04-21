package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.userPermisionBusiness.create.CreateUserPermissionBusinessCommand;
import com.kynsof.identity.application.command.userPermisionBusiness.create.CreateUserPermissionBusinessMessage;
import com.kynsof.identity.application.command.userPermisionBusiness.create.UserPermissionBusinessRequest;
import com.kynsof.identity.application.query.userPermissionBusiness.getPermissionsForUserAndBusiness.GetPermissionsForUserAndBusinessQuery;
import com.kynsof.identity.application.query.userPermissionBusiness.getPermissionsForUserAndBusiness.GetPermissionsForUserAndBusinessResponse;
import com.kynsof.identity.application.query.userPermissionBusiness.getbyid.FindByIdUserRoleBusinessQuery;
import com.kynsof.identity.application.query.userPermissionBusiness.getbyid.UserRoleBusinessResponse;
import com.kynsof.identity.application.query.userPermissionBusiness.search.GetSearchUserRolBusinessQuery;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user-permission-business")
public class UserPermissionBusinessController {

    private final IMediator mediator;

    public UserPermissionBusinessController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@RequestBody UserPermissionBusinessRequest request) {
        CreateUserPermissionBusinessCommand command = CreateUserPermissionBusinessCommand.fromRequest(request);
        CreateUserPermissionBusinessMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }


    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {

        FindByIdUserRoleBusinessQuery query = new FindByIdUserRoleBusinessQuery(id);
        UserRoleBusinessResponse response = mediator.send(query);

        return ResponseEntity.ok(response);
    }


    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetSearchUserRolBusinessQuery query = new GetSearchUserRolBusinessQuery(pageable, request.getFilter(), request.getQuery());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{userId}/business/{businessId}")
    public ResponseEntity<?> getPermissionsForUserAndBusiness(
            @PathVariable UUID userId,
            @PathVariable UUID businessId) {
        GetPermissionsForUserAndBusinessQuery query = new GetPermissionsForUserAndBusinessQuery(userId, businessId);
        GetPermissionsForUserAndBusinessResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

}
