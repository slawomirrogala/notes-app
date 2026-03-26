package com.betacom.betacom.repository;

import com.betacom.betacom.entity.Item;
import com.betacom.betacom.entity.ItemPermission;
import com.betacom.betacom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemPermissionRepository extends JpaRepository<ItemPermission, UUID> {

    List<ItemPermission> findByUserLogin(String login);
    Optional<ItemPermission> findByItemIdAndUserId(UUID itemId, UUID userId);
    Optional<ItemPermission> findByItemIdAndUserLogin(UUID itemId, String login);
}
