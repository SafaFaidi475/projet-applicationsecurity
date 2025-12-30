package xyz.kaaniche.phoenix.iam.boundaries;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import xyz.kaaniche.phoenix.iam.controllers.PhoenixIAMRepository;
import xyz.kaaniche.phoenix.iam.entities.Grant;
import xyz.kaaniche.phoenix.iam.entities.Identity;
import xyz.kaaniche.phoenix.iam.entities.Tenant;
import xyz.kaaniche.phoenix.iam.security.Argon2Utility;
import xyz.kaaniche.phoenix.iam.security.AuthorizationCode;

import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;


@Path("/")
@RequestScoped
public class AuthenticationEndpoint {
    public static final String CHALLENGE_RESPONSE_COOKIE_ID = "signInId";
    @Inject
    private Logger logger;

    @Inject
    PhoenixIAMRepository phoenixIAMRepository;

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/authorize")
    public Response authorize(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
        //1. Check tenant
        String clientId = params.getFirst("client_id");
        if (clientId == null || clientId.isEmpty()) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        Tenant tenant = phoenixIAMRepository.findTenantByName(clientId);
        if (tenant == null) {
            return informUserAboutError("Invalid client_id :" + clientId);
        }
        //2. Client Authorized Grant Type
        if (tenant.getSupportedGrantTypes() != null && !tenant.getSupportedGrantTypes().contains("authorization_code")) {
            return informUserAboutError("Authorization Grant type, authorization_code, is not allowed for this tenant :" + clientId);
        }
        //3. redirectUri
        String redirectUri = params.getFirst("redirect_uri");
        if (tenant.getRedirectUri() != null && !tenant.getRedirectUri().isEmpty()) {
            if (redirectUri != null && !redirectUri.isEmpty() && !tenant.getRedirectUri().equals(redirectUri)) {
                //should be in the client.redirectUri
                return informUserAboutError("redirect_uri is pre-registered and should match");
            }
            redirectUri = tenant.getRedirectUri();
        } else {
            if (redirectUri == null || redirectUri.isEmpty()) {
                return informUserAboutError("redirect_uri is not pre-registered and should be provided");
            }
        }

        //4. response_type
        String responseType = params.getFirst("response_type");
        if (!"code".equals(responseType) && !"token".equals(responseType)) {
            String error = "invalid_grant :" + responseType + ", response_type params should be code or token";
            return informUserAboutError(error);
        }

        //5. check scope
        String requestedScope = params.getFirst("scope");
        if (requestedScope == null || requestedScope.isEmpty()) {
            requestedScope = tenant.getRequiredScopes();
        }
        //6. code_challenge_method must be S256
        String codeChallengeMethod = params.getFirst("code_challenge_method");
        if(codeChallengeMethod==null || !codeChallengeMethod.equals("S256")){
            String error = "invalid_grant :" + codeChallengeMethod + ", code_challenge_method must be 'S256'";
            return informUserAboutError(error);
        }
        
        // Store all necessary parameters in cookie
        String codeChallenge = params.getFirst("code_challenge");
        String state = params.getFirst("state");
        String cookieValue = tenant.getName() + "#" + requestedScope + "$" + redirectUri + 
                           "$" + responseType + "$" + codeChallenge + "$" + (state != null ? state : "");
        
        StreamingOutput stream = output -> {
            try (InputStream is = Objects.requireNonNull(getClass().getResource("/login.html")).openStream()){
                output.write(is.readAllBytes());
            }
        };
        return Response.ok(stream).location(uriInfo.getBaseUri().resolve("/login/authorization"))
                .cookie(new NewCookie.Builder(CHALLENGE_RESPONSE_COOKIE_ID)
                .httpOnly(true).secure(true).sameSite(NewCookie.SameSite.STRICT).value(cookieValue).build()).build();
    }

    @POST
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public Response login(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                          @FormParam("username")String username,
                          @FormParam("password")String password,
                          @Context UriInfo uriInfo) throws Exception {
        
        if (cookie == null || cookie.getValue() == null) {
            return informUserAboutError("Invalid session. Please try again.");
        }
        
        Identity identity = phoenixIAMRepository.findIdentityByUsername(username);
        if(identity == null) {
            logger.info("Identity not found: " + username);
            String[] cookieParts = cookie.getValue().split("\\$");
            URI location = UriBuilder.fromUri(cookieParts[1])
                    .queryParam("error", "access_denied")
                    .queryParam("error_description", "Invalid credentials")
                    .build();
            return Response.seeOther(location).build();
        }
        
        if(Argon2Utility.check(identity.getPassword(),password.toCharArray())){
            logger.info("Authenticated identity:"+username);
            String[] cookieParts = cookie.getValue().split("\\$");
            String clientId = cookieParts[0].split("#")[0];
            String requestedScope = cookieParts[0].split("#")[1];
            String redirectUri = cookieParts[1];
            String responseType = cookieParts[2];
            String codeChallenge = cookieParts[3];
            String state = cookieParts.length > 4 ? cookieParts[4] : null;
            
            Optional<Grant> grant = phoenixIAMRepository.findGrant(clientId, identity.getId());
            if(grant.isPresent()){
                String redirectURI = buildActualRedirectURI(
                        redirectUri, responseType,
                        clientId,
                        identity.getId(),
                        checkUserScopes(grant.get().getApprovedScopes(), requestedScope),
                        codeChallenge, state
                );
                return Response.seeOther(UriBuilder.fromUri(redirectURI).build()).build();
            }else{
                StreamingOutput stream = output -> {
                    try (InputStream is = Objects.requireNonNull(getClass().getResource("/consent.html")).openStream()){
                        output.write(is.readAllBytes());
                    }
                };
                return Response.ok(stream).build();
            }
        } else {
            logger.info("Failure when authenticating identity:"+username);
            String[] cookieParts = cookie.getValue().split("\\$");
            URI location = UriBuilder.fromUri(cookieParts[1])
                    .queryParam("error", "access_denied")
                    .queryParam("error_description", "Invalid credentials")
                    .build();
            return Response.seeOther(location).build();
        }
    }

    @PATCH
    @Path("/login/authorization")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response grantConsent(@CookieParam(CHALLENGE_RESPONSE_COOKIE_ID) Cookie cookie,
                                 @FormParam("approved_scope") String scope,
                                 @FormParam("approval_status") String approvalStatus,
                                 @FormParam("username") String username){
        
        if (cookie == null || cookie.getValue() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid session").build();
        }
        
        String[] cookieParts = cookie.getValue().split("\\$");
        if (cookieParts.length < 4) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid session data").build();
        }
        
        String[] clientAndScope = cookieParts[0].split("#");
        String clientId = clientAndScope[0];
        String redirectUri = cookieParts[1];
        String responseType = cookieParts[2];
        String codeChallenge = cookieParts[3];
        String state = cookieParts.length > 4 && !cookieParts[4].isEmpty() ? cookieParts[4] : null;
        
        if ("NO".equals(approvalStatus)) {
            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("error", "access_denied")
                    .queryParam("error_description", "User denied the request")
                    .build();
            return Response.seeOther(location).build();
        }
        //==> YES
        if (scope == null || scope.trim().isEmpty()) {
            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("error", "invalid_scope")
                    .queryParam("error_description", "No scopes approved")
                    .build();
            return Response.seeOther(location).build();
        }
        
        List<String> approvedScopes = Arrays.stream(scope.split(" "))
                .filter(s -> !s.isEmpty())
                .toList();
        if (approvedScopes.isEmpty()) {
            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("error", "invalid_scope")
                    .queryParam("error_description", "No scopes approved")
                    .build();
            return Response.seeOther(location).build();
        }
        
        // Find identity by username
        Identity identity = phoenixIAMRepository.findIdentityByUsername(username);
        if (identity == null) {
            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("error", "access_denied")
                    .queryParam("error_description", "Invalid user")
                    .build();
            return Response.seeOther(location).build();
        }
        
        try {
            // Save the grant for future use
            phoenixIAMRepository.saveGrant(clientId, identity.getId(), String.join(" ", approvedScopes));
            
            return Response.seeOther(UriBuilder.fromUri(buildActualRedirectURI(
                    redirectUri, responseType,
                    clientId, identity.getId(), String.join(" ", approvedScopes), 
                    codeChallenge, state
            )).build()).build();
        } catch (Exception e) {
            logger.severe("Error building redirect URI: " + e.getMessage());
            URI location = UriBuilder.fromUri(redirectUri)
                    .queryParam("error", "server_error")
                    .queryParam("error_description", "Internal server error")
                    .build();
            return Response.seeOther(location).build();
        }
    }

    private String buildActualRedirectURI(String redirectUri,String responseType,String clientId,String userId,String approvedScopes,String codeChallenge,String state) throws Exception {
        StringBuilder sb = new StringBuilder(redirectUri);
        if ("code".equals(responseType)) {
            AuthorizationCode authorizationCode = new AuthorizationCode(clientId,userId,
                    approvedScopes, Instant.now().plus(2, ChronoUnit.MINUTES).getEpochSecond(),redirectUri);
            sb.append("?code=").append(URLEncoder.encode(authorizationCode.getCode(codeChallenge), StandardCharsets.UTF_8));
        } else {
            //Implicit: responseType=token : Not Supported
            return null;
        }
        if (state != null && !state.isEmpty()) {
            sb.append("&state=").append(URLEncoder.encode(state, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String checkUserScopes(String userScopes, String requestedScope) {
        Set<String> allowedScopes = new LinkedHashSet<>();
        Set<String> rScopes = new HashSet<>(Arrays.asList(requestedScope.split(" ")));
        Set<String> uScopes = new HashSet<>(Arrays.asList(userScopes.split(" ")));
        for (String scope : uScopes) {
            if (rScopes.contains(scope)) allowedScopes.add(scope);
        }
        return String.join(" ", allowedScopes);
    }

    private Response informUserAboutError(String error) {
        return Response.status(Response.Status.BAD_REQUEST).entity("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8"/>
                    <title>Error</title>
                </head>
                <body>
                <aside class="container">
                    <p>%s</p>
                </aside>
                </body>
                </html>
                """.formatted(error)).build();
    }
}