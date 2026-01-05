package com.secureteam.approvals;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/approvals")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApprovalWorkflowResource {

    private final List<AccessRequest> requests = new ArrayList<>();

    @POST
    @Path("/request")
    public Response requestAccess(AccessRequest request) {
        request.setId(UUID.randomUUID().toString());
        request.setStatus("PENDING");
        requests.add(request);
        return Response.status(Response.Status.ACCEPTED).entity(request).build();
    }

    @GET
    @Path("/pending")
    public List<AccessRequest> getPendingRequests() {
        return requests.stream()
                .filter(r -> "PENDING".equals(r.getStatus()))
                .toList();
    }

    @POST
    @Path("/approve/{id}")
    public Response approveRequest(@PathParam("id") String id) {
        return requests.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(r -> {
                    r.setStatus("APPROVED");
                    return Response.ok(r).build();
                })
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    public static class AccessRequest {
        private String id;
        private String requester;
        private String project;
        private String duration; // e.g., "2h", "1d"
        private String status;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRequester() {
            return requester;
        }

        public void setRequester(String requester) {
            this.requester = requester;
        }

        public String getProject() {
            return project;
        }

        public void setProject(String project) {
            this.project = project;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
