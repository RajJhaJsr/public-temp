package it.pkg.handler;


import it.pkg.dto.SmokeTestRequestDto;
import it.pkg.service.SmokeTestService;
import it.pkg.validator.SmokeTestValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SmokeTestHandler {

    private static final Logger logger = LoggerFactory.getLogger(SmokeTestHandler.class);

    private final SmokeTestService service;
    private final SmokeTestValidator validator;

    public SmokeTestHandler(SmokeTestService service, SmokeTestValidator validator) {
        this.service = service;
        this.validator = validator;
    }


    public Mono<ServerResponse> getActiveSmokeTests(ServerRequest request) {
        logger.info("Handling get active smoke tests request from: {}", request.remoteAddress());

        return service.getActiveSmokeTests()
                .collectList()
                .flatMap(activeTests ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(activeTests)
                )
                .doOnSuccess(response -> logger.info("Active smoke tests response sent successfully"));
    }




    public Mono<ServerResponse> handleSmokeTest(ServerRequest request) {
        logger.info("Handling smoke test request from: {}", request.remoteAddress());

        ServerWebExchange exchange = request.exchange();
        return request.bodyToMono(SmokeTestRequestDto.class)
                .doOnNext(dto -> validator.validateWithExchange(dto, exchange))
                .flatMap(service::processSmokeTest)
                .flatMap(response ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(response)
                )
                .doOnSuccess(response -> logger.info("Smoke test response sent successfully"));
    }

}