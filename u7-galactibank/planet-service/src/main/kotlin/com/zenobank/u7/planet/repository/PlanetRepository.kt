package com.zenobank.u7.planet.repository

import com.zenobank.u7.planet.entity.Planet
import io.quarkus.mongodb.panache.PanacheMongoRepository
import jakarta.enterprise.context.ApplicationScoped
import java.util.*

@ApplicationScoped
class PlanetRepository : PanacheMongoRepository<Planet> {
    fun findByPlanetId(planetId: UUID): Planet? = find("planetId", planetId).firstResult()
    fun existsByPlanetId(planetId: UUID): Boolean = count("planetId", planetId) > 0
    fun findByName(name: String): Planet? = find("name", name).firstResult()
    fun findAllActive(): List<Planet> = list("status", "ACTIVE")
}