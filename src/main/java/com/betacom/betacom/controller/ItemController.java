package com.betacom.betacom.controller;

import com.betacom.betacom.dto.item.ItemHistoryResponse;
import com.betacom.betacom.dto.item.ItemListResponse;
import com.betacom.betacom.dto.item.ItemPatchRequest;
import com.betacom.betacom.dto.item.ItemRequest;
import com.betacom.betacom.dto.item.ItemResponse;
import com.betacom.betacom.dto.item.ItemShareRequest;
import com.betacom.betacom.dto.item.ItemShareResponse;
import com.betacom.betacom.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest itemRequest, Principal principal) {
        var itemResponse = itemService.createItem(itemRequest, principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(itemResponse);
    }

    @GetMapping
    public ResponseEntity<List<ItemListResponse>> getItems(Principal principal) {
        return ResponseEntity.ok(itemService.getItems(principal.getName()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> patch(
            @PathVariable UUID id,
            @Valid @RequestBody ItemPatchRequest dto,
            Authentication authentication) throws AccessDeniedException {
        return ResponseEntity.ok(itemService.patchItem(id, dto, authentication.getName()));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<ItemShareResponse> share(
            @PathVariable UUID id,
            @Valid @RequestBody ItemShareRequest dto,
            Principal principal) throws AccessDeniedException {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.shareItem(id, dto, principal.getName()));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ItemHistoryResponse>> getHistory(
            @PathVariable UUID id,
            Principal principal) {
        return ResponseEntity.ok(itemService.getItemHistory(id, principal.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Principal principal) throws AccessDeniedException {
        itemService.softDeleteItem(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/share/{userId}")
    public ResponseEntity<Void> revoke(@PathVariable UUID id, @PathVariable UUID userId, Principal principal) throws AccessDeniedException {
        itemService.revokeAccess(id, userId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
