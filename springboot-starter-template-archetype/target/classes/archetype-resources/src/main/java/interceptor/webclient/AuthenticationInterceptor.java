#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.interceptor.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationInterceptor implements ExchangeFilterFunction {

    private final String apiToken;

    public AuthenticationInterceptor(@Value("${symbol_dollar}{app.client.http.services.serviceA.token}") String apiToken) {
        this.apiToken = apiToken;
    }

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        ClientRequest authenticatedRequest = ClientRequest.from(request)
            .header("Authorization", "Bearer " + apiToken)
            // Or for API Key: .header("X-API-Key", apiToken)
            .build();
        
        return next.exchange(authenticatedRequest);
    }
}