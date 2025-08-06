package it.pkg.interceptor.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.ConnectException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Component
public class RetryInterceptor implements ExchangeFilterFunction {

    private static final Logger logger = LoggerFactory.getLogger(RetryInterceptor.class);

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        return next.exchange(request)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> {
                            // Retry on connection issues, timeouts, and 5xx server errors
                            return throwable instanceof ConnectException ||
                                    throwable instanceof TimeoutException ||
                                    (throwable instanceof WebClientResponseException &&
                                            ((WebClientResponseException) throwable).getStatusCode().is5xxServerError());
                        })
                        .doBeforeRetry(retrySignal ->
                                logger.warn("Retrying request {} {} - Attempt: {}",
                                        request.method(), request.url(), retrySignal.totalRetries() + 1))
                )
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // For 4xx client errors, don't retry and return the response
                    if (ex.getStatusCode().is4xxClientError()) {
                        return Mono.just(ClientResponse.create(ex.getStatusCode())
                                .headers(headers -> headers.addAll(ex.getHeaders()))
                                .body(ex.getResponseBodyAsString())
                                .build());
                    }
                    // For other errors, propagate the exception
                    return Mono.error(ex);
                });
    }
}