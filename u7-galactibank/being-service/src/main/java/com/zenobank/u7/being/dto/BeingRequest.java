package com.zenobank.u7.being.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class BeingRequest {

    @NotBlank(message = "fullName is required")
    @JsonProperty("full_name")
    public String fullName;

    @NotBlank(message = "birthDate is required")
    @JsonProperty("birth_date")
    public String birthDate;

    @NotNull(message = "originPlanetId is required")
    @JsonProperty("origin_planet_id")
    public UUID originPlanetId;

    @JsonProperty("species_label")
    public String speciesLabel;
}