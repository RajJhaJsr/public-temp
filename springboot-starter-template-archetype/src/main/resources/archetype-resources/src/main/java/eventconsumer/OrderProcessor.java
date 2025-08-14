#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.eventconsumer;

import ${package}.dto.Order;
import ${package}.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class OrderProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OrderProcessor.class);

    @Bean
    public Consumer<Message<Event<Order>>> processOrder() {
        logger.debug("=== processOrder Consumer Bean Created ===");
        return message -> {
            logger.debug("=== RECEIVED MESSAGE: {} ===", message);
            Event<Order> event = message.getPayload();
            Order order = event.data();
            String orderId = order.getOrderId();
            try {
                logger.info("Processing order: {}", order);
                //processOrderService(order);
                logger.info("Order processed successfully: {}", orderId);
            } catch (Exception e) {
                logger.error("Unexpected error processing order: {}", orderId, e);
            } finally {
                MDC.clear();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T> T getHeaderValue(Message<?> message, String headerName, Class<T> type, T defaultValue) {
        try {
            Object value = message.getHeaders().get(headerName);
            if (value != null && type.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
            if (value != null && type == Integer.class && value instanceof String) {
                return (T) Integer.valueOf((String) value);
            }
        } catch (Exception e) {
            logger.warn("Error extracting header {}: {}", headerName, e.getMessage());
        }
        return defaultValue;
    }

}