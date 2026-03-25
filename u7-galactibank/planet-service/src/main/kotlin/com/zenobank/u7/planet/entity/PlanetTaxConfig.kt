package com.zenobank.u7.planet.entity

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@MongoEntity(collection = "planet_tax_configs")
class PlanetTaxConfig(
    var planetId: UUID,
    var taxRate: BigDecimal,
    var taxType: String,
    var configId: UUID = UUID.randomUUID(),
    var validFrom: LocalDateTime = LocalDateTime.now(),
    var validUntil: LocalDateTime? = null
) : PanacheMongoEntity()