package com.InterestRatesAustria.InterestRatesAustria.security.persistance;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User, Long> {
    User getUserByUsername(@Param("username") String username);
}