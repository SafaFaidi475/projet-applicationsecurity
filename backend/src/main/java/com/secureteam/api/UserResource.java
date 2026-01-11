package com.secureteam.api;

import com.secureteam.model.User;
import com.secureteam.model.Role;
import com.secureteam.model.Project;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class UserResource {

    @PersistenceContext
    private EntityManager em;

    @GET
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @GET
    @Path("/{id}")
    public User getUser(@PathParam("id") Long id) {
        return em.find(User.class, id);
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, User user) {
        User existing = em.find(User.class, id);
        if (existing == null)
            return Response.status(Response.Status.NOT_FOUND).build();

        existing.setDepartment(user.getDepartment());
        existing.setRoles(user.getRoles());
        existing.setAuthorizedProjects(user.getAuthorizedProjects());
        em.merge(existing);

        return Response.ok(existing).build();
    }
}
