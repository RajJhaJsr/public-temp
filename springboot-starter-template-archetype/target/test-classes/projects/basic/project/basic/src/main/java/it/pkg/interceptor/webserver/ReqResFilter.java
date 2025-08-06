// src/main/java/com/example/webflux/filter/GlobalResponseFilter.java
package it.pkg.interceptor.webserver;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

@Component
//TODO: Trace-ID from OpenTelemetry (MDC)
public class ReqResFilter implements WebFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(ReqResFilter.class);

    // Static resources to exclude from logging
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/favicon.ico",
            "/robots.txt",
            "/sitemap.xml"
    );

    // Path patterns to exclude
    private static final Set<String> EXCLUDED_PATH_PATTERNS = Set.of(
            "/.well-known/",
            "/static/",
            "/assets/",
            "/css/",
            "/js/",
            "/images/",
            "/webjars/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Skip entire filter processing for static resources and browser-specific requests
        if (shouldSkipRequest(path)) {
            // Return 404 directly without going through the filter chain
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.NOT_FOUND);
            logger.info("{} is a static resource, returning 404", path);
            return response.setComplete();
        }

        String method = exchange.getRequest().getMethod().name();

        long startTime = System.currentTimeMillis();
        String traceId = UUID.randomUUID().toString().substring(0, 8);

        // Store trace ID and start time in exchange attributes
        exchange.getAttributes().put("traceId", traceId);
        exchange.getAttributes().put("startTime", startTime);

        logger.info("[{}] Incoming request: {} {}", traceId, method, path);

        // Decorate response if it's an API path
        if (shouldDecorateResponse(path)) {
            ResponseDecorator decoratedResponse = new ResponseDecorator(exchange.getResponse(), traceId, startTime);
            ServerWebExchange decoratedExchange = exchange.mutate()
                    .response(decoratedResponse)
                    .build();

            logger.debug("[{}] Decorating response for path: {}", traceId, path);

            return chain.filter(decoratedExchange)
                    .doFinally(signalType -> logCompletion(traceId, method, path, decoratedResponse, startTime));
        }

        // For non-API paths, just log without decoration
        return chain.filter(exchange)
                .doFinally(signalType -> logCompletion(traceId, method, path, exchange.getResponse(), startTime));
    }

    private boolean shouldSkipRequest(String path) {
        // Skip exact path matches
        if (EXCLUDED_PATHS.contains(path)) {
            return true;
        }

        // Skip path pattern matches
        return EXCLUDED_PATH_PATTERNS.stream()
                .anyMatch(pattern -> path.startsWith(pattern));
    }

    private void logCompletion(String traceId, String method, String path,
                               org.springframework.http.server.reactive.ServerHttpResponse response,
                               long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        int statusCode = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        logger.info("[{}] Completed request: {} {} - Status: {} - Duration: {}ms",
                traceId, method, path, statusCode, duration);
    }

    private boolean shouldDecorateResponse(String path) {
        return path.startsWith("/api/") &&
                !path.contains("/actuator/") &&
                !path.contains("/swagger") &&
                !path.contains("/api-docs");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}
