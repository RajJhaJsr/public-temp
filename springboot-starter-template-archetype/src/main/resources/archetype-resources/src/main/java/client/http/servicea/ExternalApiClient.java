#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.client.http.servicea;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ExternalApiClient {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiClient.class);

    private final String serviceAUrl;
    private final WebClient webClient;

    public ExternalApiClient(@Value("${symbol_dollar}{app.client.http.services.serviceA.url}") String serviceAUrl, WebClient webClient) {
        this.serviceAUrl = serviceAUrl;
        this.webClient = webClient;
    }


    public Mono<ExternalApiResponseDto> fetchExternalData() {
        logger.info("Fetching data from external API: {}", serviceAUrl);

        return webClient.get()
                .uri(serviceAUrl)
                .retrieve()
                .bodyToMono(ExternalApiResponseDto.class)
                .doOnSuccess(response -> logger.info("Successfully fetched external data: {}", response))
                .doOnError(error -> logger.error("Error fetching external data", error));
    }
}