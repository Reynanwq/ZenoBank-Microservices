package com.zenobank.u7.planet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotBlank

data class PlanetRequest(
    @get:NotBlank(message = "name is required")
    @JsonProperty("name")
    val name: String,

    @JsonProperty("galaxy")
    val galaxy: String? = null,

    @JsonProperty("population")
    val population: Long = 0
)