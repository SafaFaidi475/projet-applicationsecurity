package com.secureteam.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter, ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (requestContext.getMethod().equalsIgnoreCase("OPTIONS")) {
            String origin = requestContext.getHeaderString("Origin");
            if (origin == null) {
                origin = "*";
            }
            Response response = Response.ok()
                    .header("Access-Control-Allow-Origin", origin)
                    .header("Access-Control-Allow-Credentials", "true")
                    .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                    .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                    .build();
            requestContext.abortWith(response);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        String origin = requestContext.getHeaderString("Origin");
        if (origin == null) {
            origin = "*";
        }
        // Force replace if already set erroneously
        responseContext.getHeaders().remove("Access-Control-Allow-Origin");
        responseContext.getHeaders().add("Access-Control-Allow-Origin", origin);

        responseContext.getHeaders().add("Access-Control-Allow-Credentials", "true");
        responseContext.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
        responseContext.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}
