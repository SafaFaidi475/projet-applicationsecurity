package com.secureteam.api;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class AuditResource {

    private final List<Map<String, Object>> logs = new ArrayList<>();

    public void logAccess(String user, String resource, boolean granted, String reason) {
        Map<String, Object> log = new HashMap<>();
        log.put("timestamp", System.currentTimeMillis());
        log.put("user", user);
        log.put("resource", resource);
        log.put("granted", granted);
        log.put("reason", reason);
        logs.add(log);
    }

    @GET
    public Response getLogs() {
        return Response.ok(logs).build();
    }
}
