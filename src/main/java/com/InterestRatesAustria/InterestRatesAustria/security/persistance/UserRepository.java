package com.InterestRatesAustria.InterestRatesAustria.security.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    User getUserByUsername(@Param("username") String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByVerificationToken(String verificationToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.username = :email")
    Optional<User> findByEmail(@Param("email") String email);

    // New methods for admin functionality
    List<User> findByEmailVerifiedTrueAndEnabledFalse(); // Pending approval

    List<User> findByEnabledTrue(); // Enabled users

    List<User> findByEmailVerifiedFalse(); // Unverified users

    long countByEmailVerifiedTrueAndEnabledFalse(); // Count pending users

    long countByEnabledTrue(); // Count enabled users

    long countByEmailVerifiedFalse(); // Count unverified users
}