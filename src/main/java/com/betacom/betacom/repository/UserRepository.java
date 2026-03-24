package com.betacom.betacom.repository;

import com.betacom.betacom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    boolean existsByLogin(String login);
    Optional<User> findByLogin(String login);
}
