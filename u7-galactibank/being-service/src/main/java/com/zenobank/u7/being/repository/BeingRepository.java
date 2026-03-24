package com.zenobank.u7.being.repository;

import com.zenobank.u7.being.entity.Being;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class BeingRepository implements PanacheMongoRepository<Being> {

    public Optional<Being> findByUid(UUID uid) {
        return find("uid", uid).firstResultOptional();
    }

    public boolean existsByUid(UUID uid) {
        return count("uid", uid) > 0;
    }

    public Optional<Being> findByFullName(String fullName) {
        return find("fullName", fullName).firstResultOptional();
    }
}