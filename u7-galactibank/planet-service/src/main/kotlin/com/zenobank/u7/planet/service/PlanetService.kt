package com.zenobank.u7.planet.service

import com.zenobank.u7.planet.dto.*
import com.zenobank.u7.planet.entity.Planet
import com.zenobank.u7.planet.entity.enum.PlanetStatus
import com.zenobank.u7.planet.entity.PlanetTaxConfig
import com.zenobank.u7.planet.entity.enum.TaxType
import com.zenobank.u7.planet.repository.PlanetRepository
import com.zenobank.u7.planet.repository.PlanetTaxConfigRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.NotFoundException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

@ApplicationScoped
class PlanetService {

    @Inject
    lateinit var planetRepository: PlanetRepository

    @Inject
    lateinit var taxConfigRepository: PlanetTaxConfigRepository

    // Planet CRUD
    fun createPlanet(request: PlanetRequest): PlanetResponse {
        val planet = Planet(
            name = request.name,
            galaxy = request.galaxy,
            population = request.population
        )
        planetRepository.persist(planet)

        return PlanetResponse(
            planetId = planet.planetId,
            name = planet.name,
            galaxy = planet.galaxy,
            population = planet.population,
            status = planet.status.name,
            createdAt = planet.createdAt
        )
    }

    fun getPlanet(planetId: UUID): PlanetResponse {
        val planet = planetRepository.findByPlanetId(planetId)
            ?: throw NotFoundException("Planet not found with ID: $planetId")

        return PlanetResponse(
            planetId = planet.planetId,
            name = planet.name,
            galaxy = planet.galaxy,
            population = planet.population,
            status = planet.status.name,
            createdAt = planet.createdAt
        )
    }

    fun updatePlanet(planetId: UUID, request: PlanetRequest): PlanetResponse {
        val planet = planetRepository.findByPlanetId(planetId)
            ?: throw NotFoundException("Planet not found with ID: $planetId")

        planet.name = request.name
        planet.galaxy = request.galaxy
        planet.population = request.population
        planetRepository.update(planet)

        return PlanetResponse(
            planetId = planet.planetId,
            name = planet.name,
            galaxy = planet.galaxy,
            population = planet.population,
            status = planet.status.name,
            createdAt = planet.createdAt
        )
    }

    fun deletePlanet(planetId: UUID) {
        val planet = planetRepository.findByPlanetId(planetId)
            ?: throw NotFoundException("Planet not found with ID: $planetId")

        planet.status = PlanetStatus.INACTIVE
        planetRepository.update(planet)
    }

    fun destroyPlanet(planetId: UUID) {
        val planet = planetRepository.findByPlanetId(planetId)
            ?: throw NotFoundException("Planet not found with ID: $planetId")

        planet.status = PlanetStatus.DESTROYED
        planetRepository.update(planet)
    }

    // Tax Config
    fun setTaxConfig(request: TaxConfigRequest): TaxConfigResponse {
        val taxType = try {
            TaxType.valueOf(request.taxType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid tax type: ${request.taxType}. Must be OUTBOUND or INBOUND")
        }

        val activeConfigs = taxConfigRepository.findActiveConfigs(request.planetId, taxType)
        activeConfigs.forEach { it.validUntil = LocalDateTime.now() }
        if (activeConfigs.isNotEmpty()) {
            taxConfigRepository.persist(activeConfigs)
        }

        val config = PlanetTaxConfig(
            planetId = request.planetId,
            taxRate = request.taxRate,
            taxType = taxType
        )
        taxConfigRepository.persist(config)

        return TaxConfigResponse(
            configId = config.configId,
            planetId = config.planetId,
            taxRate = config.taxRate,
            taxType = config.taxType.name,
            validFrom = config.validFrom,
            validUntil = config.validUntil
        )
    }

    fun getCurrentTaxRate(planetId: UUID, taxType: String): BigDecimal {
        val taxTypeEnum = try {
            TaxType.valueOf(taxType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid tax type: $taxType. Must be OUTBOUND or INBOUND")
        }

        return taxConfigRepository.findCurrentTaxRate(planetId, taxTypeEnum)
            ?: throw NotFoundException("No active tax config found for planet $planetId and type $taxType")
    }
}