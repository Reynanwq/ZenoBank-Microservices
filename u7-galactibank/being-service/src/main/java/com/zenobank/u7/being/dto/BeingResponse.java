package com.zenobank.u7.being.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.UUID;

public class BeingResponse {

    public UUID uid;

    @JsonProperty("full_name")
    public String fullName;

    @JsonProperty("birth_date")
    public String birthDate;

    @JsonProperty("origin_planet_id")
    public UUID originPlanetId;

    @JsonProperty("species_label")
    public String speciesLabel;

    public String status;

    public String meta;

    @JsonProperty("created_at")
    public LocalDateTime createdAt;

    public BeingResponse(UUID uid, String fullName, String birthDate, UUID originPlanetId,
                         String speciesLabel, String status, String meta, LocalDateTime createdAt) {
        this.uid = uid;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.originPlanetId = originPlanetId;
        this.speciesLabel = speciesLabel;
        this.status = status;
        this.meta = meta;
        this.createdAt = createdAt;
    }
}