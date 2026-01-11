package com.secureteam.api;

import com.secureteam.model.Project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class ProjectResource {

    @PersistenceContext
    private EntityManager em;

    @GET
    public List<Project> getAllProjects() {
        return em.createQuery("SELECT p FROM Project p", Project.class).getResultList();
    }

    @POST
    public Response createProject(Project project) {
        em.persist(project);
        return Response.status(Response.Status.CREATED).entity(project).build();
    }

    @GET
    @Path("/{id}")
    public Project getProject(@PathParam("id") Long id) {
        return em.find(Project.class, id);
    }
}
