package com.zenobank.u7.planet.resource

import com.zenobank.u7.planet.dto.PlanetRequest
import com.zenobank.u7.planet.dto.PlanetResponse
import com.zenobank.u7.planet.dto.TaxConfigRequest
import com.zenobank.u7.planet.dto.TaxConfigResponse
import com.zenobank.u7.planet.service.PlanetService
import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.net.URI
import java.util.*

@Path("/api/planets")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class PlanetResource {

    @Inject
    lateinit var service: PlanetService

    @POST
    fun createPlanet(@Valid request: PlanetRequest): Response {
        val response = service.createPlanet(request)
        return Response.created(URI.create("/api/planets/${response.planetId}"))
            .entity(response)
            .build()
    }

    @GET
    @Path("/{planetId}")
    fun getPlanet(@PathParam("planetId") planetId: UUID): PlanetResponse {
        return service.getPlanet(planetId)
    }

    @PUT
    @Path("/{planetId}")
    fun updatePlanet(@PathParam("planetId") planetId: UUID, @Valid request: PlanetRequest): PlanetResponse {
        return service.updatePlanet(planetId, request)
    }

    @DELETE
    @Path("/{planetId}")
    fun deletePlanet(@PathParam("planetId") planetId: UUID): Response {
        service.deletePlanet(planetId)
        return Response.noContent().build()
    }

    @POST
    @Path("/{planetId}/destroy")
    fun destroyPlanet(@PathParam("planetId") planetId: UUID): Response {
        service.destroyPlanet(planetId)
        return Response.noContent().build()
    }

    @POST
    @Path("/tax-config")
    fun setTaxConfig(@Valid request: TaxConfigRequest): TaxConfigResponse {
        return service.setTaxConfig(request)
    }

    @GET
    @Path("/{planetId}/tax-rate/{taxType}")
    fun getCurrentTaxRate(
        @PathParam("planetId") planetId: UUID,
        @PathParam("taxType") taxType: String
    ): Map<String, Any> {
        val rate = service.getCurrentTaxRate(planetId, taxType)
        return mapOf(
            "planet_id" to planetId,
            "tax_type" to taxType,
            "tax_rate" to rate
        )
    }
}