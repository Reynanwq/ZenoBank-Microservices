package com.zenobank.u7.planet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class TaxConfigResponse(
    @JsonProperty("config_id")
    val configId: UUID,

    @JsonProperty("planet_id")
    val planetId: UUID,

    @JsonProperty("tax_rate")
    val taxRate: BigDecimal,

    @JsonProperty("tax_type")
    val taxType: String,

    @JsonProperty("valid_from")
    val validFrom: LocalDateTime,

    @JsonProperty("valid_until")
    val validUntil: LocalDateTime?
)