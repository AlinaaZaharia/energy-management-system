package ro.tuc.gateway.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (isPublicEndpoint(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "No token provided");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> validationResponse = restTemplate.exchange(
                    authServiceUrl + "/api/auth/validate",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            Map<String, Object> userData = validationResponse.getBody();

            if (userData != null && Boolean.TRUE.equals(userData.get("valid"))) {
                request.setAttribute("userId", userData.get("userId"));
                request.setAttribute("username", userData.get("username"));
                request.setAttribute("role", userData.get("role"));

                filterChain.doFilter(request, response);
            } else {
                sendUnauthorizedResponse(response, "Invalid token");
            }
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            sendUnauthorizedResponse(response, "Token validation failed");
        }
    }

    private boolean isPublicEndpoint(String path) {
        return path.contains("/api/auth/login")
                || path.contains("/api/auth/register")
                || path.equals("/api/auth/health")
                || path.contains("/api/auth/credentials");
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}