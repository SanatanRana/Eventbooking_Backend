package com.ranadj.repository;

import com.ranadj.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for CRUD operations on the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieve a user by email address.
     *
     * @param email user email
     * @return Optional of User
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email.
     *
     * @param email user email
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);
}
