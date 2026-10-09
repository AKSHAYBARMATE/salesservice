package com.projectmanagement.seller.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.projectmanagement.seller.common.StandardResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class JwtExtractionFilter implements Filter {

    private final ObjectMapper mapper;
    private final LoginUser loginUser;
    private final JwtTokenProvider jwtTokenProvider;

    private static final List<String> PUBLIC_URL_PREFIXES = List.of(
            "/api/v1/sellerservice/login",
            "/api/v1/sellerservice/auth",
            "/api/v1/sellerservice/forgotPassword",
            "/api/v1/sellerservice/verifyOtp",
            "/api/v1/sellerservice/resetPassword",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/actuator",
            "/error"
    );

    public JwtExtractionFilter(LoginUser loginUser, JwtTokenProvider jwtTokenProvider) {
        this.loginUser = loginUser;
        this.jwtTokenProvider = jwtTokenProvider;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Allow CORS preflight requests
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow public endpoints without token
        if (isPublicUrl(path)) {
            try {
                // If token is optionally provided in public request, still extract context
                extractTokenIfPresent(httpRequest);
                chain.doFilter(request, response);
            } finally {
                loginUser.clear();
            }
            return;
        }

        // For protected endpoints, enforce JWT authentication
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(httpResponse, "Missing or invalid Authorization header. Expected 'Bearer <token>'.");
            return;
        }

        String token = authHeader.substring(7).trim();
        if (!jwtTokenProvider.validateToken(token)) {
            sendUnauthorizedResponse(httpResponse, "Invalid or expired JWT token.");
            return;
        }

        try {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);

            Object userIdVal = claims.get("userId");
            if (userIdVal == null) {
                userIdVal = claims.get("id");
            }
            if (userIdVal != null) {
                loginUser.setUserId(Long.valueOf(userIdVal.toString()));
            }

            String username = (String) claims.get("username");
            if (username == null || username.isBlank()) {
                username = (String) claims.get("name");
            }
            if (username == null || username.isBlank()) {
                username = claims.getSubject();
            }
            loginUser.setUsername(username);

            String email = (String) claims.get("email");
            if (email == null || email.isBlank()) {
                email = claims.getSubject();
            }
            loginUser.setEmail(email);

            chain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error processing JWT token claims: {}", e.getMessage());
            sendUnauthorizedResponse(httpResponse, "Failed to authenticate JWT token: " + e.getMessage());
        } finally {
            loginUser.clear();
        }
    }

    private boolean isPublicUrl(String path) {
        if (path == null) return false;
        for (String prefix : PUBLIC_URL_PREFIXES) {
            if (path.startsWith(prefix) || path.equals(prefix)) {
                return true;
            }
        }
        return false;
    }

    private void extractTokenIfPresent(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    Claims claims = jwtTokenProvider.getClaimsFromToken(token);
                    Object userIdVal = claims.get("userId");
                    if (userIdVal != null) {
                        loginUser.setUserId(Long.valueOf(userIdVal.toString()));
                    }
                    loginUser.setUsername((String) claims.get("username"));
                    loginUser.setEmail((String) claims.get("email"));
                } catch (Exception ignored) {
                }
            }
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        StandardResponse<Void> errorResponse = StandardResponse.error(
                message,
                "UNAUTHORIZED",
                "Full authentication is required to access this resource."
        );
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }
}
