#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.eventpublisher;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.solace.spring.cloud.stream.binder.messaging.SolaceBinderHeaders;
import com.solace.spring.cloud.stream.binder.util.CorrelationData;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private StreamBridge streamBridge;


    public boolean sendWithConfirmation(String bindingName, Object payload) throws Exception {
        return sendWithConfirmation(bindingName, payload, Duration.ofSeconds(5));
    }


    public boolean sendWithConfirmation(String bindingName, Object payload, Duration timeout) throws Exception {
        // Create correlation data for tracking
        String correlationId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData();

        // Build message with correlation header
        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader(SolaceBinderHeaders.CONFIRM_CORRELATION, correlationData)
                .setHeader("messageId", correlationId)
                .build();

        log.info("Sending message with correlation ID: {}", correlationId);

        // Send message
        boolean sent = streamBridge.send(bindingName, message);

        if (!sent) {
            log.error("StreamBridge.send() returned false for correlation ID: {}", correlationId);
            throw new RuntimeException("Failed to send message - StreamBridge returned false");
        }

        try {
            // Wait for broker confirmation
            log.debug("Waiting for broker confirmation for correlation ID: {}", correlationId);
            correlationData.getFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS);

            log.info("Message confirmed by broker for correlation ID: {}", correlationId);
            return true;

        } catch (TimeoutException e) {
            log.error("Timeout waiting for confirmation for correlation ID: {} after {}ms",
                    correlationId, timeout.toMillis());
            throw new RuntimeException("Publisher confirmation timeout after " + timeout, e);

        } catch (Exception e) {
            log.error("Publisher confirmation failed for correlation ID: {}: {}",
                    correlationId, e.getMessage());
            throw new RuntimeException("Publisher confirmation failed: " + e.getMessage(), e);
        }
    }


    public CompletableFuture<Boolean> sendWithAsyncConfirmation(String bindingName, Object payload) {
        String correlationId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData();

        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader(SolaceBinderHeaders.CONFIRM_CORRELATION, correlationData)
                .setHeader("messageId", correlationId)
                .build();

        log.info("Sending message async with correlation ID: {}", correlationId);

        boolean sent = streamBridge.send(bindingName, message);

        if (!sent) {
            log.error("StreamBridge.send() returned false for correlation ID: {}", correlationId);
            return CompletableFuture.failedFuture(
                    new RuntimeException("Failed to send message - StreamBridge returned false"));
        }

        // Convert Solace CorrelationData.Future to CompletableFuture
        return CompletableFuture.supplyAsync(() -> {
            try {
                correlationData.getFuture().get(10, TimeUnit.SECONDS);
                log.info("Message confirmed async for correlation ID: {}", correlationId);
                return true;
            } catch (Exception e) {
                log.error("Async confirmation failed for correlation ID: {}: {}",
                        correlationId, e.getMessage());
                throw new RuntimeException("Publisher confirmation failed", e);
            }
        });
    }


    public void sendBatchWithConfirmations(String bindingName, Object[] payloads) throws Exception {
        CompletableFuture<Boolean>[] futures = new CompletableFuture[payloads.length];

        // Send all messages async
        for (int i = 0; i < payloads.length; i++) {
            futures[i] = sendWithAsyncConfirmation(bindingName, payloads[i]);
        }

        // Wait for all confirmations
        try {
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
            log.info("All {} messages confirmed successfully", payloads.length);
        } catch (Exception e) {
            log.error("Batch send failed: {}", e.getMessage());
            throw new RuntimeException("Batch publisher confirmation failed", e);
        }
    }


    public void sendWithCallback(String bindingName, Object payload,
                                 ConfirmationCallback callback) {

        String correlationId = UUID.randomUUID().toString();
        CorrelationData correlationData = new CorrelationData();

        Message<Object> message = MessageBuilder
                .withPayload(payload)
                .setHeader(SolaceBinderHeaders.CONFIRM_CORRELATION, correlationData)
                .setHeader("messageId", correlationId)
                .build();

        boolean sent = streamBridge.send(bindingName, message);

        if (!sent) {
            callback.onFailure(correlationId,
                    new RuntimeException("StreamBridge send failed"));
            return;
        }

        // Handle confirmation async
        CompletableFuture.runAsync(() -> {
            try {
                correlationData.getFuture().get(10, TimeUnit.SECONDS);
                callback.onSuccess(correlationId);
            } catch (Exception e) {
                callback.onFailure(correlationId, e);
            }
        });
    }
}


class MessageCorrelationData {
    private final String correlationId;
    private final CorrelationData solaceCorrelationData;
    private final long timestamp;
    private String businessContext;

    public MessageCorrelationData(String correlationId) {
        this.correlationId = correlationId;
        this.solaceCorrelationData = new CorrelationData();
        this.timestamp = System.currentTimeMillis();
    }

    public MessageCorrelationData(String correlationId, String businessContext) {
        this(correlationId);
        this.businessContext = businessContext;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public CorrelationData getSolaceCorrelationData() {
        return solaceCorrelationData;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(String businessContext) {
        this.businessContext = businessContext;
    }

    @Override
    public String toString() {
        return String.format("MessageCorrelationData{id='%s', timestamp=%d, context='%s'}",
                correlationId, timestamp, businessContext);
    }
}


interface ConfirmationCallback {
    void onSuccess(String correlationId);
    void onFailure(String correlationId, Exception exception);
}