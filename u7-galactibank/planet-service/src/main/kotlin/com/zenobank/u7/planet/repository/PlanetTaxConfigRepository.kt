package com.zenobank.u7.planet.repository

import com.zenobank.u7.planet.entity.PlanetTaxConfig
import io.quarkus.mongodb.panache.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ApplicationScoped
class PlanetTaxConfigRepository : PanacheMongoRepository<PlanetTaxConfig> {

    fun findCurrentTaxRate(planetId: UUID, taxType: String): BigDecimal? {
        val config: PlanetTaxConfig? = find(
            "planetId = ?1 and taxType = ?2 and validFrom <= ?3 and (validUntil is null or validUntil > ?3)",
            planetId, taxType, LocalDateTime.now()
        ).firstResult()
        return config?.taxRate
    }

    fun findHistory(planetId: UUID): List<PlanetTaxConfig> {
        return find("planetId", planetId).list()
    }

    fun findActiveConfigs(planetId: UUID, taxType: String): List<PlanetTaxConfig> {
        return find("planetId = ?1 and taxType = ?2 and validUntil is null", planetId, taxType).list()
    }
}