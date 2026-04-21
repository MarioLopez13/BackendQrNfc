package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.profile.create.CreateProfileCommand;
import com.kynsof.identity.application.command.profile.create.CreateProfileMessage;
import com.kynsof.identity.application.command.profile.create.CreateProfileRequest;
import com.kynsof.identity.application.command.profile.removePermissions.RemovePermissionsFromProfileCommand;
import com.kynsof.identity.application.command.profile.removePermissions.RemovePermissionsFromProfileMessage;
import com.kynsof.identity.application.command.profile.removePermissions.RemovePermissionsFromProfileRequest;
import com.kynsof.identity.application.command.profile.update.UpdateProfileCommand;
import com.kynsof.identity.application.command.profile.update.UpdateProfileMessage;
import com.kynsof.identity.application.command.profile.update.UpdateProfileRequest;
import com.kynsof.identity.application.command.profile.delete.DeleteProfileCommand;
import com.kynsof.identity.application.command.profile.delete.DeleteProfileMessage;
import java.util.UUID;
import com.kynsof.identity.application.query.profile.getbyid.GetProfileByIdQuery;
import com.kynsof.identity.application.query.profile.search.GetSearchProfileQuery;
import com.kynsof.identity.application.query.profile.search.ProfileSearchResponse;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final IMediator mediator;

    public ProfileController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping
    public ResponseEntity<CreateProfileMessage> create(@RequestBody CreateProfileRequest request) {
        CreateProfileCommand command = CreateProfileCommand.fromRequest(request);
        CreateProfileMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileSearchResponse> getById(@PathVariable UUID id) {
        GetProfileByIdQuery query = new GetProfileByIdQuery(id);
        ProfileSearchResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UpdateProfileMessage> update(@PathVariable UUID id, @RequestBody UpdateProfileRequest request) {
        UpdateProfileCommand command = UpdateProfileCommand.fromRequest(id, request);
        UpdateProfileMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedResponse> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetSearchProfileQuery query = new GetSearchProfileQuery(pageable, request.getFilter(), request.getQuery());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }

    @DeleteMapping("/permissions")
    public ResponseEntity<RemovePermissionsFromProfileMessage> removePermissions(@RequestBody RemovePermissionsFromProfileRequest request) {
        RemovePermissionsFromProfileCommand command = RemovePermissionsFromProfileCommand.fromRequest(request);
        RemovePermissionsFromProfileMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteProfileMessage> delete(@PathVariable UUID id) {
        DeleteProfileCommand command = new DeleteProfileCommand(id);
        DeleteProfileMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }
}
