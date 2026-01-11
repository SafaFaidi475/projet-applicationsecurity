package com.secureteam.api;

import com.secureteam.auth.PasetoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Path("/keys")
@Produces(MediaType.APPLICATION_JSON)
public class KeyResource {

    @Inject
    private PasetoService pasetoService;

    @GET
    @Path("/public")
    public Map<String, String> getPublicKey() {
        Map<String, String> keys = new HashMap<>();
        keys.put("publicKey", Base64.getEncoder().encodeToString(pasetoService.getPublicKey().getEncoded()));
        keys.put("algorithm", "Ed25519");
        return keys;
    }
}
