package com.zenobank.u7.planet.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.util.UUID

data class TaxConfigRequest(
    @get:NotNull(message = "planetId is required")
    @JsonProperty("planet_id")
    val planetId: UUID,

    @get:DecimalMin(value = "0.0", inclusive = false, message = "taxRate must be greater than 0")
    @JsonProperty("tax_rate")
    val taxRate: BigDecimal,

    @get:NotBlank(message = "taxType is required")
    @JsonProperty("tax_type")
    val taxType: String  // OUTBOUND, INBOUND - virar enum
)