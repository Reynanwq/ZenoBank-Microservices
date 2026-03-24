package com.zenobank.u7.being.resource;

import com.zenobank.u7.being.dto.BeingRequest;
import com.zenobank.u7.being.dto.BeingResponse;
import com.zenobank.u7.being.service.BeingService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.UUID;

@Path("/api/beings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BeingResource {

    @Inject
    BeingService service;

    @POST
    public Response createBeing(@Valid BeingRequest request) {
        BeingResponse response = service.createBeing(request);
        return Response.created(URI.create("/api/beings/" + response.uid))
                .entity(response)
                .build();
    }

    @GET
    @Path("/{uid}")
    public Response getBeing(@PathParam("uid") UUID uid) {
        BeingResponse response = service.getBeingByUid(uid);
        return Response.ok(response).build();
    }

    @PUT
    @Path("/{uid}")
    public Response updateBeing(@PathParam("uid") UUID uid, @Valid BeingRequest request) {
        BeingResponse response = service.updateBeing(uid, request);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{uid}")
    public Response deleteBeing(@PathParam("uid") UUID uid) {
        service.deleteBeing(uid);
        return Response.noContent().build();
    }
}