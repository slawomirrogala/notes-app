package com.betacom.betacom.service;

import com.betacom.betacom.dto.item.ItemHistoryResponse;
import com.betacom.betacom.dto.item.ItemListResponse;
import com.betacom.betacom.dto.item.ItemPatchRequest;
import com.betacom.betacom.dto.item.ItemRequest;
import com.betacom.betacom.dto.item.ItemResponse;
import com.betacom.betacom.dto.item.ItemShareRequest;
import com.betacom.betacom.dto.item.ItemShareResponse;
import com.betacom.betacom.entity.Item;
import com.betacom.betacom.entity.ItemPermission;
import com.betacom.betacom.entity.User;
import com.betacom.betacom.enums.PermissionRole;
import com.betacom.betacom.exception.ItemNotFoundException;
import com.betacom.betacom.exception.ItemOrUserNotFoundException;
import com.betacom.betacom.exception.RoleNotFoundException;
import com.betacom.betacom.exception.UserNotFoundException;
import com.betacom.betacom.repository.ItemPermissionRepository;
import com.betacom.betacom.repository.ItemRepository;
import com.betacom.betacom.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemPermissionRepository itemPermissionRepository;
    private final EntityManager entityManager;

    @Transactional
    public ItemResponse createItem(ItemRequest itemRequest, String login) {
        var owner = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));

        var savedItem = saveItem(itemRequest, owner);
        saveItemPermission(savedItem, owner);

        return ItemResponse.builder()
                .id(savedItem.getId())
                .title(savedItem.getTitle())
                .content(savedItem.getContent())
                .version(savedItem.getVersion())
                .ownerId(owner.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(login)
                .build();
    }

    @Transactional
    public List<ItemListResponse> getItems(String login) {
        return itemPermissionRepository.findByUserLogin(login)
                .stream()
                .filter(itemPermission -> !itemPermission.getItem().getDeleted())
                .map(itemPermission -> new ItemListResponse(
                        itemPermission.getItem().getId(),
                        itemPermission.getItem().getTitle(),
                        itemPermission.getItem().getContent(),
                        itemPermission.getItem().getVersion(),
                        itemPermission.getItem().getOwner().getId(),
                        itemPermission.getRole().name(),
                        itemPermission.getItem().getUpdatedAt()))
                .toList();
    }

    @Transactional
    public ItemResponse patchItem(UUID itemId, ItemPatchRequest dto, String login) throws AccessDeniedException {
        var itemPermission = itemPermissionRepository.findByItemIdAndUserLogin(itemId, login)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (itemPermission.getRole().equals(PermissionRole.VIEWER)) {
            throw new AccessDeniedException("Masz tylko uprawnienia do podglądu");
        }

        var item = itemPermission.getItem();

        if (!item.getVersion().equals(dto.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Item.class, itemId);
        }

        if (dto.getTitle() != null) item.setTitle(dto.getTitle());
        if (dto.getContent() != null) item.setContent(dto.getContent());
        item.setVersion(item.getVersion() + 1);
        return mapToResponse(itemRepository.save(item), login);
    }

    @Transactional
    public void softDeleteItem(UUID itemId, String login) throws AccessDeniedException {
        var item = itemRepository.findByIdAndDeletedFalse(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (!item.getOwner().getLogin().equals(login)) {
            throw new AccessDeniedException("Tylko właściciel może usunąć notatkę");
        }

        item.setDeleted(true);
        item.setVersion(item.getVersion()+ 1);
        itemRepository.save(item);
    }

    @Transactional
    public ItemShareResponse shareItem(UUID itemId,
                                       ItemShareRequest dto,
                                       String ownerLogin) throws AccessDeniedException {
        var ownerItemPermission = itemPermissionRepository.findByItemIdAndUserLogin(itemId, ownerLogin)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        if (!ownerItemPermission.getRole().equals(PermissionRole.OWNER)) {
            throw new AccessDeniedException("Tylko właściciel może udostępniać notatkę");
        }

        var targetUser = userRepository.findById(dto.userId())
                .orElseThrow(() -> new UserNotFoundException("Użytkownik docelowy nie istnieje"));

        if (targetUser.getLogin().equals(ownerLogin)) {
            throw new IllegalArgumentException("Nie możesz udostępnić notatki samemu sobie");
        }

        if (Arrays.stream(PermissionRole.values()).noneMatch(permissionRole -> permissionRole.name().equals(dto.role()))) {
            throw new RoleNotFoundException();
        }

        ItemPermission itemPermission = ItemPermission
                .builder()
                .item(ownerItemPermission.getItem())
                .user(targetUser)
                .role(PermissionRole.valueOf(dto.role()))
                .build();

         itemPermissionRepository.save(itemPermission);
         return new ItemShareResponse(itemId, targetUser.getId(), dto.role(), LocalDateTime.now());
    }

    @Transactional
    public List<ItemHistoryResponse> getItemHistory(UUID itemId, String login) {
        var itemPermission = itemPermissionRepository.findByItemIdAndUserLogin(itemId, login)
                .orElseThrow(ItemOrUserNotFoundException::new);

        if(itemPermission.getRole() == null) {
            throw new RoleNotFoundException();
        }

        if(itemPermission.getItem().getDeleted().equals(true)) {
            throw new ItemNotFoundException(itemId);
        }

        var auditReader = AuditReaderFactory.get(entityManager);
        List<Object[]> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(Item.class, false, true)
                .add(AuditEntity.id().eq(itemId))
                .getResultList();

        return revisions.stream().map(row -> {
            Item auditedItem = (Item) row[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) row[1];
            RevisionType revisionType = (RevisionType) row[2];

            return new ItemHistoryResponse(
                    auditedItem.getId(),
                    auditedItem.getTitle(),
                    auditedItem.getContent(),
                    auditedItem.getVersion(),
                    login,
                    revisionEntity.getId(),
                    revisionType.name(),
                    LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(revisionEntity.getTimestamp()),
                            ZoneId.systemDefault()
                    )
            );
        }).toList();
    }

    @Transactional
    public void revokeAccess(UUID itemId, UUID targetUserId, String ownerLogin) throws AccessDeniedException {
        var ownerItemPermission = itemPermissionRepository.findByItemIdAndUserLogin(itemId, ownerLogin)
                .orElseThrow(ItemOrUserNotFoundException::new);

        if (!ownerItemPermission.getRole().equals(PermissionRole.OWNER)) {
            throw new AccessDeniedException("Tylko owner może zarządzać dostępem");
        }

        var permission = itemPermissionRepository.findByItemIdAndUserId(itemId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Ten użytkownik nie posiadał uprawnień do tej notatki"));
        itemPermissionRepository.delete(permission);
    }

    private Item saveItem(ItemRequest itemRequest, User owner) {
        var item = Item.builder()
                .title(itemRequest.getTitle())
                .content(itemRequest.getContent())
                .owner(owner)
                .version(0)
                .deleted(false)
                .createdBy(owner.getLogin())
                .build();

        return itemRepository.save(item);
    }

    private void saveItemPermission(Item item, User user) {
        var itemPermission = ItemPermission.builder()
                .item(item)
                .user(user)
                .role(PermissionRole.OWNER)
                .build();
        itemPermissionRepository.save(itemPermission);
    }

    private ItemResponse mapToResponse(Item item, String login) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .version(item.getVersion())
                .ownerId(item.getOwner().getId())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .createdBy(login)
                .build();
    }
}
