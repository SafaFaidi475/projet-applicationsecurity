package com.secureteam.api;

import com.secureteam.steganography.SteganographyService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Path("/stego")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SteganographyResource {

    @Inject
    private SteganographyService steganographyService;

    @POST
    @Path("/hide")
    public Response hideData(StegoRequest request) {
        try {
            byte[] coverImage = Base64.getDecoder().decode(request.getCoverImage());
            byte[] message = request.getMessage().getBytes();

            byte[] stegoImage = steganographyService.embed(coverImage, message);

            Map<String, String> response = new HashMap<>();
            response.put("stegoImage", Base64.getEncoder().encodeToString(stegoImage));
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/extract")
    public Response extractData(StegoRequest request) {
        try {
            byte[] stegoImage = Base64.getDecoder().decode(request.getCoverImage());
            byte[] messageBytes = steganographyService.extract(stegoImage);

            Map<String, String> response = new HashMap<>();
            response.put("message", new String(messageBytes));
            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
        }
    }

    public static class StegoRequest {
        private String coverImage; // Base64
        private String message;

        public String getCoverImage() {
            return coverImage;
        }

        public void setCoverImage(String coverImage) {
            this.coverImage = coverImage;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
