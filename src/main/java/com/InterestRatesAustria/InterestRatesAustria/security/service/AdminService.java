package com.InterestRatesAustria.InterestRatesAustria.security.service;

import com.InterestRatesAustria.InterestRatesAustria.security.persistance.User;
import com.InterestRatesAustria.InterestRatesAustria.security.persistance.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }

    public List<User> getPendingUsers() {
        return userRepository.findByEmailVerifiedTrueAndEnabledFalse();
    }

    public List<User> getEnabledUsers() {
        return userRepository.findByEnabledTrue();
    }

    public List<User> getUnverifiedUsers() {
        return userRepository.findByEmailVerifiedFalse();
    }

    public boolean enableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (!user.isEmailVerified()) {
            throw new IllegalStateException("Cannot enable user - email not verified");
        }

        user.setEnabled(true);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail());

        return true;
    }

    public boolean disableUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.setEnabled(false);
        userRepository.save(user);

        emailService.sendAccountDisabledNotification(user.getEmail());

        return true;
    }

    public boolean deleteUser(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (userOpt.isEmpty()) {
            return false;
        }

        userRepository.deleteById(userId);
        return true;
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getPendingUsersCount() {
        return userRepository.countByEmailVerifiedTrueAndEnabledFalse();
    }

    public long getEnabledUsersCount() {
        return userRepository.countByEnabledTrue();
    }

    public long getUnverifiedUsersCount() {
        return userRepository.countByEmailVerifiedFalse();
    }
}