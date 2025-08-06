#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.interceptor.webserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

@Component
public class SecurityFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    ;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/actuator/health",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/api-docs",
            "/webjars/",
            "/api/smoketest/"
    );

    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://hsbc.com"
    );

    private static final List<String> REQUIRED_HEADERS = List.of(
            "X-API-Version",
            "Content-Type"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();
        String method = request.getMethod().name();

        // Add security headers to all responses
        addSecurityHeaders(response);

        // Skip security checks for public paths
        if (isPublicPath(path)) {
            logger.debug("Allowing public access to: {}", path);
            return chain.filter(exchange);
        }

        // CORS validation
        if (!isValidCorsRequest(request)) {
            logger.warn("CORS validation failed for origin: {}", request.getHeaders().getOrigin());
            return unauthorized(response, "Invalid origin");
        }

        // Authentication validation
        if (!isAuthenticated(request)) {
            logger.warn("Authentication failed for path: {}", path);
            return unauthorized(response, "Authentication required");
        }

        // API Version validation
        if (!hasValidApiVersion(request)) {
            logger.warn("Invalid or missing API version for path: {}", path);
            return badRequest(response, "Valid X-API-Version header required");
        }

        // Content-Type validation for POST/PUT requests
        if (requiresContentType(method) && !hasValidContentType(request)) {
            logger.warn("Invalid Content-Type for {} request to: {}", method, path);
            return badRequest(response, "Valid Content-Type header required");
        }

        // Rate limiting check (basic implementation)
        if (isRateLimited(request)) {
            logger.warn("Rate limit exceeded for IP: {}", getClientIp(request));
            return tooManyRequests(response, "Rate limit exceeded");
        }

        logger.debug("Security validation passed for: {} {}", method, path);
        return chain.filter(exchange);
    }

    private void addSecurityHeaders(ServerHttpResponse response) {
        HttpHeaders headers = response.getHeaders();

        // Security headers
        headers.add("X-Content-Type-Options", "nosniff");
        headers.add("X-Frame-Options", "DENY");
        headers.add("X-XSS-Protection", "1; mode=block");
        headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        headers.add("Referrer-Policy", "strict-origin-when-cross-origin");
        headers.add("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        // CORS headers
        headers.add("Access-Control-Allow-Origin", "*"); // Configure properly for production
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization, X-API-Version, X-Trace-ID");
        headers.add("Access-Control-Max-Age", "3600");
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private boolean isValidCorsRequest(ServerHttpRequest request) {
        String origin = request.getHeaders().getOrigin();

        // Allow requests without origin (same-origin requests)
        if (origin == null) {
            return true;
        }

        // Check against allowed origins
        return ALLOWED_ORIGINS.contains(origin) || ALLOWED_ORIGINS.contains("*");
    }

    private boolean isAuthenticated(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        return true; // Implement your authentication logic here
    }

    private boolean hasValidApiVersion(ServerHttpRequest request) {
        String apiVersion = request.getHeaders().getFirst("X-API-Version");

        // For API paths, version is mandatory
        if (request.getPath().value().startsWith("/api/")) {
            return StringUtils.hasText(apiVersion) &&
                    (apiVersion.equals("v1") || apiVersion.equals("${version}"));
        }

        return true;
    }

    private boolean requiresContentType(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private boolean hasValidContentType(ServerHttpRequest request) {
        String contentType = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);

        return StringUtils.hasText(contentType) &&
                (contentType.contains(MediaType.APPLICATION_JSON_VALUE) ||
                        contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE));
    }

    private boolean isRateLimited(ServerHttpRequest request) {
        String clientIp = getClientIp(request);
        return false; // Implement your RateLimit logic here
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddress() != null ?
                request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }

    private Mono<Void> unauthorized(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("WWW-Authenticate", "Basic realm=${symbol_escape}"API${symbol_escape}"");
        return writeErrorResponse(response, message);
    }

    private Mono<Void> badRequest(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.BAD_REQUEST);
        return writeErrorResponse(response, message);
    }

    private Mono<Void> tooManyRequests(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().add("Retry-After", "60");
        return writeErrorResponse(response, message);
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String message) {
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String errorJson = String.format(
                "{${symbol_escape}"error${symbol_escape}":${symbol_escape}"%s${symbol_escape}",${symbol_escape}"status${symbol_escape}":%d,${symbol_escape}"timestamp${symbol_escape}":${symbol_escape}"%s${symbol_escape}"}",
                message,
                response.getStatusCode().value(),
                java.time.Instant.now().toString()
        );

        org.springframework.core.io.buffer.DataBuffer buffer =
                response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));

        return response.writeWith(reactor.core.publisher.Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}