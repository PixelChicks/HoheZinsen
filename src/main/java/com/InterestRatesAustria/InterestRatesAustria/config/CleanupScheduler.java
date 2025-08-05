package com.InterestRatesAustria.InterestRatesAustria.config;

import com.InterestRatesAustria.InterestRatesAustria.repository.UserRepository;
import com.InterestRatesAustria.InterestRatesAustria.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task to clean up expired verification tokens and unverified users.
 */
@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?") // Run every day at 2 AM
    @Transactional
    public void cleanupExpiredTokensAndUsers() {
        log.info("Starting cleanup of expired tokens and unverified users");

        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // Delete expired verification tokens
        var expiredTokens = verificationTokenRepository.findByExpiryDateBefore(LocalDateTime.now());
        verificationTokenRepository.deleteAll(expiredTokens);
        log.info("Deleted {} expired verification tokens", expiredTokens.size());

        // Delete users who registered more than 24 hours ago but never verified their email
        var unverifiedUsers = userRepository.findByCreatedAtBeforeAndEmailVerifiedFalse(yesterday);
        
        // Delete their tokens first
        for (var user : unverifiedUsers) {
            verificationTokenRepository.deleteByUser(user);
        }
        
        userRepository.deleteAll(unverifiedUsers);
        log.info("Deleted {} unverified users older than 24 hours", unverifiedUsers.size());

        log.info("Cleanup completed");
    }
}