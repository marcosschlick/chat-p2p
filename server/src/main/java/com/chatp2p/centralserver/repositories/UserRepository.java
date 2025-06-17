package com.chatp2p.centralserver.repositories;

import com.chatp2p.centralserver.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.online = true")
    List<User> findOnlineUsers();

    @Modifying
    @Query("UPDATE User u SET u.online = :online WHERE u.id = :id")
    void updateOnlineStatus(@Param("id") Long id, @Param("online") Boolean online);
}
