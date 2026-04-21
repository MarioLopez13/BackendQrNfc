package com.kynsof.identity.controller;

import com.kynsof.identity.application.command.advertisementImage.create.CreateAdvertisementImageCommand;
import com.kynsof.identity.application.command.advertisementImage.create.CreateAdvertisementImageMessage;
import com.kynsof.identity.application.command.advertisementImage.create.CreateAdvertisementImageRequest;
import com.kynsof.identity.application.command.advertisementImage.delete.DeleteAdvertisementImageCommand;
import com.kynsof.identity.application.command.advertisementImage.delete.DeleteAdvertisementImageMessage;
import com.kynsof.identity.application.command.advertisementImage.update.UpdateAdvertisementImageCommand;
import com.kynsof.identity.application.command.advertisementImage.update.UpdateAdvertisementImageMessage;
import com.kynsof.identity.application.command.advertisementImage.update.UpdateAdvertisementImageRequest;
import com.kynsof.identity.application.query.advertisementImage.getById.FindAdvertisementImageByIdQuery;
import com.kynsof.identity.application.query.advertisementImage.search.AdvertisementImageResponse;
import com.kynsof.identity.application.query.advertisementImage.search.GetSearchAdvertisementImageQuery;
import com.kynsof.share.core.domain.request.PageableUtil;
import com.kynsof.share.core.domain.request.SearchRequest;
import com.kynsof.share.core.domain.response.PaginatedResponse;
import com.kynsof.share.core.infrastructure.bus.IMediator;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/advertisement-image")
public class AdvertisementImageController {

    private final IMediator mediator;

    public AdvertisementImageController(IMediator mediator) {
        this.mediator = mediator;
    }

    @PostMapping()
    public ResponseEntity<CreateAdvertisementImageMessage> create(@RequestBody CreateAdvertisementImageRequest request) {
        CreateAdvertisementImageCommand command = CreateAdvertisementImageCommand.fromRequest(request);
        CreateAdvertisementImageMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<?> search(@RequestBody SearchRequest request) {
        Pageable pageable = PageableUtil.createPageable(request);
        GetSearchAdvertisementImageQuery query = new GetSearchAdvertisementImageQuery(pageable, request.getFilter());
        PaginatedResponse data = mediator.send(query);
        return ResponseEntity.ok(data);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<?> getById(@PathVariable UUID id) {
        FindAdvertisementImageByIdQuery query = new FindAdvertisementImageByIdQuery(id);
        AdvertisementImageResponse response = mediator.send(query);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        DeleteAdvertisementImageCommand command = new DeleteAdvertisementImageCommand(id);
        DeleteAdvertisementImageMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id, @RequestBody UpdateAdvertisementImageRequest request) {
        UpdateAdvertisementImageCommand command = UpdateAdvertisementImageCommand.fromRequest(id, request);
        UpdateAdvertisementImageMessage response = mediator.send(command);
        return ResponseEntity.ok(response);
    }
}
