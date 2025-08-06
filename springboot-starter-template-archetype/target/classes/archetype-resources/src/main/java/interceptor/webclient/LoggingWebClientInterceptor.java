#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.interceptor.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;


@Component
public class LoggingWebClientInterceptor implements ExchangeFilterFunction {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingWebClientInterceptor.class);
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        long startTime = System.currentTimeMillis();
        
        logger.info("Request: {} {}", request.method(), request.url());
        logger.debug("Request Headers: {}", request.headers());
        
        return next.exchange(request)
            .doOnNext(response -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("Response: {} {} - Status: {} - Duration: {}ms", 
                    request.method(), request.url(), response.statusCode(), duration);
                logger.debug("Response Headers: {}", response.headers().asHttpHeaders());
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                logger.error("Request failed: {} {} - Duration: {}ms - Error: {}", 
                    request.method(), request.url(), duration, error.getMessage());
            });
    }
}