#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.service;


import ${package}.client.http.servicea.ExternalApiResponseDto;
import ${package}.client.http.servicea.ExternalApiClient;
import ${package}.constants.AppConstants;
import ${package}.dto.Order;
import ${package}.dto.SmokeTestRequestDto;
import ${package}.dto.SmokeTestResponseDto;
import ${package}.entity.SmokeTestEntity;
import ${package}.event.AggregateType;
import ${package}.event.Event;
import ${package}.event.EventType;
import ${package}.event.Status;
import ${package}.eventpublisher.EventPublisher;
import ${package}.maper.SmokeTestMapper;
import ${package}.repository.SmokeTestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;


@Service
public class SmokeTestService {

    private static final Logger logger = LoggerFactory.getLogger(SmokeTestService.class);

    private final SmokeTestRepository repository;
    private final SmokeTestMapper mapper;
    private final ExternalApiClient externalApiService;
    private final EventPublisher eventPublisher;
    private final String applicationName;
    private final String processOrderOutDestination;

    public SmokeTestService(SmokeTestRepository repository,
                            SmokeTestMapper mapper,
                            ExternalApiClient externalApiService,
                            EventPublisher eventPublisher,
                            @Value("${symbol_dollar}{spring.application.name}") String applicationName,
                            @Value("${symbol_dollar}{spring.cloud.stream.bindings.processOrder-out-0.destination}") String processOrderOutDestination) {
        this.repository = repository;
        this.mapper = mapper;
        this.externalApiService = externalApiService;
        this.eventPublisher = eventPublisher;
        this.applicationName = applicationName;
        this.processOrderOutDestination = processOrderOutDestination;
    }

    public Flux<SmokeTestResponseDto> getActiveSmokeTests() {
        logger.info("Fetching all active smoke test records");

        return repository.findByStatus(AppConstants.DEFAULT_STATUS)
                .map(entity -> {
                    SmokeTestResponseDto baseResponse = mapper.toResponseDto(entity);
                    return new SmokeTestResponseDto(
                            baseResponse.id(),
                            baseResponse.name(),
                            baseResponse.description(),
                            baseResponse.status(),
                            null, // No external data for list view
                            baseResponse.createdAt(),
                            "Active record retrieved"
                    );
                })
                .doOnComplete(() -> logger.info("Successfully fetched all active smoke test records"));
    }


    public Mono<SmokeTestResponseDto> processSmokeTest(SmokeTestRequestDto requestDto) {
        logger.info("Processing smoke test for: {}", requestDto.name());

        return Mono.fromCallable(() -> {
                    SmokeTestEntity entity = mapper.toEntity(requestDto);
                    entity.setStatus(AppConstants.DEFAULT_STATUS);
                    return entity;
                })
                .flatMap(repository::save)
                .flatMap(savedEntity ->
                        externalApiService.fetchExternalData()
                                .map(externalData -> createResponseDto(savedEntity, externalData))
                                .defaultIfEmpty(createResponseDto(savedEntity, null))
                )
                .doOnSuccess(response -> {
                    publishOrderEvent();
                    logger.info("Smoke test processed successfully: {}", response.id());
                });
    }

    private void publishOrderEvent() {
        Order order = new Order("ORD-12345", "AAPL", 100, new BigDecimal("145.50"), "BUY");

        Event<Order> event = Event.of(
                EventType.ORDER_PLACED,
                AggregateType.ORDER,
                order.getOrderId(),
                Instant.now(),
                10248,
                "TXN-99887766",
                "CMD-123",
                applicationName,
                "v1",
                Status.SUCCESS,
                Map.of("ipAddress", "192.168.0.5", "region", "US"),
                order
        );
        try {
            logger.debug("Publishing Order event: " + event.eventId());
            eventPublisher.sendWithConfirmation(processOrderOutDestination, event);
        } catch (Exception e) {
            logger.error("Failed to publish Order event: " + e.getMessage(), e);
        }
    }

    private SmokeTestResponseDto createResponseDto(SmokeTestEntity entity, ExternalApiResponseDto externalData) {
        SmokeTestResponseDto baseResponse = mapper.toResponseDto(entity);
        return new SmokeTestResponseDto(
                baseResponse.id(),
                baseResponse.name(),
                baseResponse.description(),
                baseResponse.status(),
                externalData,
                baseResponse.createdAt(),
                AppConstants.SUCCESS_MESSAGE
        );
    }
}