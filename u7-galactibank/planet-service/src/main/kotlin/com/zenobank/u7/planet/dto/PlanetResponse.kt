package com.zenobank.u7.planet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
import java.util.UUID

data class PlanetResponse(
    @JsonProperty("planet_id")
    val planetId: UUID,

    @JsonProperty("name")
    val name: String,

    @JsonProperty("galaxy")
    val galaxy: String?,

    @JsonProperty("population")
    val population: Long,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("created_at")
    val createdAt: LocalDateTime
)