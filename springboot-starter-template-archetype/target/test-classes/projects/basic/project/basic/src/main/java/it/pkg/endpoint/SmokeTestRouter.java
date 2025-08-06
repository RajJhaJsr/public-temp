package it.pkg.endpoint;


import it.pkg.constants.AppConstants;
import it.pkg.handler.SmokeTestHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class SmokeTestRouter {

    @Bean
    public RouterFunction<ServerResponse> smokeTestRoutes(SmokeTestHandler handler) {
        return RouterFunctions
                .route(POST(AppConstants.API_BASE_PATH + AppConstants.SMOKE_TEST_PATH)
                                .and(accept(MediaType.APPLICATION_JSON)),
                        handler::handleSmokeTest)
                .andRoute(GET(AppConstants.API_BASE_PATH + AppConstants.SMOKE_TEST_PATH + "/active")
                                .and(accept(MediaType.APPLICATION_JSON)),
                        handler::getActiveSmokeTests);
    }
}