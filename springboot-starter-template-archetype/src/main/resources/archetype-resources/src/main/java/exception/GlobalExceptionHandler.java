#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.exception;

import ${package}.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@Order(0)
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Autowired
    private MessageSource messageSource;

    // Message keys for different error types
    private static final String INTERNAL_SERVER_ERROR_KEY = "error.internal.server";
    private static final String BAD_REQUEST_KEY = "error.bad.request";
    private static final String VALIDATION_ERROR_KEY = "error.validation";
    private static final String NOT_FOUND_KEY = "error.not.found";
    private static final String UNAUTHORIZED_KEY = "error.unauthorized";
    private static final String FORBIDDEN_KEY = "error.forbidden";
    private static final String METHOD_NOT_ALLOWED_KEY = "error.method.not.allowed";
    private static final String UNSUPPORTED_MEDIA_TYPE_KEY = "error.unsupported.media.type";
    private static final String SERVICE_UNAVAILABLE_KEY = "error.service.unavailable";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        logger.error("Global exception handler caught: ", ex);

        Locale locale = getLocaleFromRequest(exchange);
        HttpStatus status = determineHttpStatus(ex);
        String localizedMessage = getLocalizedErrorMessage(ex, status, locale);

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);

        ApiException apiException = new ApiException(
                status.value(),
                localizedMessage,
                getErrorCode(ex),
                getErrorDetails(ex, locale)
        );

        DataBuffer buffer = exchange.getResponse().bufferFactory()
                .wrap(JsonUtil.toJson(apiException).getBytes(StandardCharsets.UTF_8));

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private Locale getLocaleFromRequest(ServerWebExchange exchange) {
        // Try to get locale from Accept-Language header
        String acceptLanguage = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        if (acceptLanguage != null && !acceptLanguage.isEmpty()) {
            try {
                return Locale.forLanguageTag(acceptLanguage.split(",")[0].trim());
            } catch (Exception e) {
                logger.debug("Failed to parse Accept-Language header: {}", acceptLanguage);
            }
        }

        // Try to get locale from query parameter
        String langParam = exchange.getRequest().getQueryParams().getFirst("lang");
        if (langParam != null && !langParam.isEmpty()) {
            try {
                return Locale.forLanguageTag(langParam);
            } catch (Exception e) {
                logger.debug("Failed to parse lang parameter: {}", langParam);
            }
        }

        // Default to English
        return Locale.ENGLISH;
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ServiceException) {
            ServiceException serviceEx = (ServiceException) ex;
            // Assume ServiceException has a getStatus() method
            return serviceEx.getStatus() != null ? serviceEx.getStatus() : HttpStatus.BAD_REQUEST;
        } else if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        } else if (ex instanceof WebExchangeBindException || ex instanceof BindException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        }

        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String getLocalizedErrorMessage(Throwable ex, HttpStatus status, Locale locale) {
        String messageKey = getMessageKeyForStatus(status);
        String defaultMessage = status.getReasonPhrase();

        // For ServiceException, try to get specific message key
        if (ex instanceof ServiceException) {
            ServiceException serviceEx = (ServiceException) ex;
            String specificKey = serviceEx.getMessageKey();
            if (specificKey != null) {
                return messageSource.getMessage(specificKey, serviceEx.getMessageArgs(),
                        defaultMessage, locale);
            }
        }

        // For validation errors, create detailed message
        if (ex instanceof WebExchangeBindException) {
            return getValidationErrorMessage((WebExchangeBindException) ex, locale);
        }

        // Use generic message key for the status
        return messageSource.getMessage(messageKey, null, defaultMessage, locale);
    }

    private String getMessageKeyForStatus(HttpStatus status) {
        switch (status) {
            case BAD_REQUEST:
                return BAD_REQUEST_KEY;
            case UNAUTHORIZED:
                return UNAUTHORIZED_KEY;
            case FORBIDDEN:
                return FORBIDDEN_KEY;
            case NOT_FOUND:
                return NOT_FOUND_KEY;
            case METHOD_NOT_ALLOWED:
                return METHOD_NOT_ALLOWED_KEY;
            case UNSUPPORTED_MEDIA_TYPE:
                return UNSUPPORTED_MEDIA_TYPE_KEY;
            case SERVICE_UNAVAILABLE:
                return SERVICE_UNAVAILABLE_KEY;
            default:
                return INTERNAL_SERVER_ERROR_KEY;
        }
    }

    private String getValidationErrorMessage(WebExchangeBindException ex, Locale locale) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> messageSource.getMessage(
                        "validation." + error.getField() + "." + error.getCode(),
                        error.getArguments(),
                        error.getDefaultMessage(),
                        locale
                ))
                .collect(Collectors.joining(", "));

        String baseMessage = messageSource.getMessage(VALIDATION_ERROR_KEY, null,
                "Validation failed", locale);

        return baseMessage + ": " + validationErrors;
    }

    private String getErrorCode(Throwable ex) {
        if (ex instanceof ServiceException) {
            return ((ServiceException) ex).getErrorCode();
        } else if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getStatusCode().toString();
        }
        return "INTERNAL_ERROR";
    }

    private String getErrorDetails(Throwable ex, Locale locale) {
        // Only include stack trace details in development
        if (isDevelopmentMode()) {
            Throwable cause = ex.getCause();
            //String st = Arrays.stream(ex.getStackTrace()).toList().toString();
            String details = cause != null ? cause.toString() : ex.toString();
            return details;
        }

        // In production, return generic localized message
        return messageSource.getMessage("error.contact.support", null,
                "Please contact support if the problem persists.", locale);
    }

    private boolean isDevelopmentMode() {
        // For example, check if spring.profiles.active contains "dev"
        return true;
    }
}