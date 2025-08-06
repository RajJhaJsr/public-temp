// src/main/java/com/example/webflux/decorator/ResponseDecorator.java
package it.pkg.interceptor.webserver;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseDecorator extends ServerHttpResponseDecorator {

    private final String traceId;
    private final long startTime;

    public ResponseDecorator(ServerHttpResponse delegate, String traceId, long startTime) {
        super(delegate);
        this.traceId = traceId;
        this.startTime = startTime;
    }


    @Override
    public Mono<Void> writeWith(org.reactivestreams.Publisher<? extends DataBuffer> body) {
        // Add headers
        getHeaders().add("X-Trace-ID", traceId);
        getHeaders().add("X-Response-Time", (System.currentTimeMillis() - startTime) + "ms");
        getHeaders().add("X-Timestamp", Instant.now().toString());
        
//        if (body instanceof Flux) {
//            Flux<DataBuffer> fluxBody = (Flux<DataBuffer>) body;
//            return super.writeWith(fluxBody.map(this::decorateResponseBody));
//        } else if (body instanceof Mono) {
//            Mono<DataBuffer> monoBody = (Mono<DataBuffer>) body;
//            return super.writeWith(monoBody.map(this::decorateResponseBody));
//        }
        
        return super.writeWith(body);
    }
    
    private DataBuffer decorateResponseBody(DataBuffer buffer) {
        try {
            String originalBody = buffer.toString(StandardCharsets.UTF_8);
            DataBufferUtils.release(buffer);
            
            // Only decorate JSON responses
            if (isJsonResponse(originalBody)) {
                String decoratedBody = addTimestampToJson(originalBody);
                return bufferFactory().wrap(decoratedBody.getBytes(StandardCharsets.UTF_8));
            }
            
            return bufferFactory().wrap(originalBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // If decoration fails, return original buffer
            return buffer;
        }
    }
    
    private boolean isJsonResponse(String body) {
        return body.trim().startsWith("{") || body.trim().startsWith("[");
    }
    
    private String addTimestampToJson(String originalJson) {
        if (originalJson.trim().startsWith("{") && originalJson.trim().endsWith("}")) {
            // Single JSON object
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String traceInfo = String.format("\"trace_id\":\"%s\",\"response_time\":\"%sms\",\"server_timestamp\":\"%s\"", 
                    traceId, 
                    (System.currentTimeMillis() - startTime), 
                    timestamp);
            
            // Insert before the closing brace
            int lastBraceIndex = originalJson.lastIndexOf("}");
            if (lastBraceIndex > 0) {
                String beforeBrace = originalJson.substring(0, lastBraceIndex);
                if (!beforeBrace.trim().endsWith("{")) {
                    beforeBrace += ",";
                }
                return beforeBrace + traceInfo + "}";
            }
        } else if (originalJson.trim().startsWith("[")) {
            // JSON array - wrap in metadata object
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return String.format("{\"data\":%s,\"trace_id\":\"%s\",\"response_time\":\"%sms\",\"server_timestamp\":\"%s\"}", 
                    originalJson, traceId, (System.currentTimeMillis() - startTime), timestamp);
        }
        
        return originalJson;
    }
}