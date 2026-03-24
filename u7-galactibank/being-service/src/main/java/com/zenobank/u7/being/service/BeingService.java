package com.zenobank.u7.being.service;

import com.zenobank.u7.being.dto.BeingRequest;
import com.zenobank.u7.being.dto.BeingResponse;
import com.zenobank.u7.being.entity.Being;
import com.zenobank.u7.being.repository.BeingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@ApplicationScoped
public class BeingService {

    @Inject
    BeingRepository repository;

    // Sem @Transactional
    public BeingResponse createBeing(BeingRequest request) {
        Being being = new Being(
                request.fullName,
                request.birthDate,
                request.originPlanetId,
                request.speciesLabel != null ? request.speciesLabel : "UNKNOWN"
        );

        repository.persist(being);

        return new BeingResponse(
                being.uid,
                being.fullName,
                being.birthDate,
                being.originPlanetId,
                being.speciesLabel,
                being.status,
                being.meta,
                being.createdAt
        );
    }

    public BeingResponse getBeingByUid(UUID uid) {
        Being being = repository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Being not found with UID: " + uid));

        return new BeingResponse(
                being.uid,
                being.fullName,
                being.birthDate,
                being.originPlanetId,
                being.speciesLabel,
                being.status,
                being.meta,
                being.createdAt
        );
    }

    // Sem @Transactional
    public BeingResponse updateBeing(UUID uid, BeingRequest request) {
        Being being = repository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Being not found with UID: " + uid));

        being.fullName = request.fullName;
        being.birthDate = request.birthDate;
        being.originPlanetId = request.originPlanetId;
        being.speciesLabel = request.speciesLabel != null ? request.speciesLabel : being.speciesLabel;

        repository.update(being);

        return new BeingResponse(
                being.uid,
                being.fullName,
                being.birthDate,
                being.originPlanetId,
                being.speciesLabel,
                being.status,
                being.meta,
                being.createdAt
        );
    }

    // Sem @Transactional
    public void deleteBeing(UUID uid) {
        Being being = repository.findByUid(uid)
                .orElseThrow(() -> new NotFoundException("Being not found with UID: " + uid));

        // Soft delete - muda status para INACTIVE
        being.status = "INACTIVE";
        repository.update(being);
    }
}