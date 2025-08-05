package com.InterestRatesAustria.InterestRatesAustria.repository;

import com.InterestRatesAustria.InterestRatesAustria.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByEmailVerifiedTrueAndEnabledFalse();
    
    List<User> findByEnabledTrue();
    
    List<User> findByCreatedAtBeforeAndEmailVerifiedFalse(LocalDateTime dateTime);
}

