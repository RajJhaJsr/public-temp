#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.event;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record Event<T>(
        UUID eventId,
        EventType eventType,
        AggregateType aggregateType,
        String aggregateId,
        Instant eventOccurrenceTime, // business action time
        Instant eventProcessingTime, // processed/published time
        long sequenceNumber,
        String correlationId,
        String causationId,
        String sourceSystem,
        String version,
        int retryCount,
        Status status,
        String errorCode,
        String errorMessage,
        Map<String, Object> metadata, // Dynamic metadata
        T data
) {

    public static <T> Event<T> of(
            EventType eventType,
            AggregateType aggregateType,
            String aggregateId,
            Instant eventOccurrenceTime,
            long sequenceNumber,
            String correlationId,
            String causationId,
            String sourceSystem,
            String version,
            Status status,
            Map<String, Object> metadata,
            T data
    ) {
        return new Event<>(
                UUID.randomUUID(),
                eventType,
                aggregateType,
                aggregateId,
                eventOccurrenceTime,
                Instant.now(),
                sequenceNumber,
                correlationId,
                causationId,
                sourceSystem,
                version,
                0,
                status,
                null,
                null,
                metadata,
                data
        );
    }
}


/*

{
        "eventId": "d98f1c72-43a9-45e0-b3aa-37ab1202a190",
        "eventType": "ORDER_PLACED",
        "aggregateType": "ORDER",
        "aggregateId": "ORD-12345",
        "eventOccurrenceTime": "2025-08-13T10:15:00Z",
        "sequenceNumber": 10248,
        "correlationId": "TXN-99887766",
        "causationId": "CMD-123",
        "sourceSystem": "OrderService",
        "version": 1,
        "retryCount": 0,
        "status": "SUCCESS",
        "errorCode": null,
        "errorMessage": null,
        "metadata": {
        "riskLevel": "LOW",
        "traderId": "TRD-4521",
        "omsRoute": "PRIMARY",
        "debugFlag": false,
        "settlementWindow": "T+2"
        },
        "data": {
        "orderId": "ORD-12345",
        "symbol": "AAPL",
        "side": "BUY",
        "quantity": 100,
        "price": 195.50
        }
}

*/
