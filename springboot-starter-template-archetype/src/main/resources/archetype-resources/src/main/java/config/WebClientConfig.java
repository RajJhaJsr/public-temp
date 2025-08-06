#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.config;

import ${package}.interceptor.webclient.AuthenticationInterceptor;
import ${package}.interceptor.webclient.LoggingWebClientInterceptor;
import ${package}.interceptor.webclient.RetryInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Configuration
public class WebClientConfig {

    @Autowired
    private LoggingWebClientInterceptor loggingInterceptor;

    @Autowired
    private AuthenticationInterceptor authInterceptor;

    @Autowired
    private RetryInterceptor retryInterceptor;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .filter(loggingInterceptor)
                .filter(authInterceptor)
                .filter(retryInterceptor)
                .build();
    }
}