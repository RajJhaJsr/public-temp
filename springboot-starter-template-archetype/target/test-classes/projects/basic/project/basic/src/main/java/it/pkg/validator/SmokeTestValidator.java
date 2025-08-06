package it.pkg.validator;

import it.pkg.dto.SmokeTestRequestDto;
import it.pkg.exception.ServiceException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.*;
import java.util.stream.Collectors;

import static it.pkg.constants.ErrorCodes.VALIDATION_FIELD_FORMAT;

@Component
public class SmokeTestValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(SmokeTestValidator.class);
    
    @Autowired
    private Validator validator;
    
    @Autowired
    private MessageSource messageSource;
    
    public void validate(SmokeTestRequestDto dto) {
        validate(dto, Locale.ENGLISH);
    }
    
    public void validate(SmokeTestRequestDto dto, Locale locale) {
        Set<ConstraintViolation<SmokeTestRequestDto>> violations = validator.validate(dto);
        
        if (!violations.isEmpty()) {
            String localizedErrors = violations.stream()
                    .map(violation -> getLocalizedViolationMessage(violation, locale))
                    .collect(Collectors.joining(", "));
            
            // Throw ServiceException with localized validation message key
            throw new ServiceException(HttpStatus.BAD_REQUEST, VALIDATION_FIELD_FORMAT.getCode(),VALIDATION_FIELD_FORMAT.getMessageKey(), new Object[]{localizedErrors});
        }
    }
    
    public void validateWithExchange(SmokeTestRequestDto dto, ServerWebExchange exchange) {
        Locale locale = getLocaleFromExchange(exchange);
        validate(dto, locale);
    }
    
    private String getLocalizedViolationMessage(ConstraintViolation<SmokeTestRequestDto> violation, Locale locale) {
        String field = violation.getPropertyPath().toString();
        String constraintType = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
        
        // Try specific validation message key first
        String specificKey = "validation." + field + "." + constraintType;
        
        try {
            // Try to get localized message with constraint arguments
            Object[] args = getConstraintArguments(violation);
            return messageSource.getMessage(specificKey, args, violation.getMessage(), locale);
        } catch (Exception e) {
            logger.debug("No specific validation message found for key: {}, using default", specificKey);
        }
        
        // Try generic constraint type message
        String genericKey = "validation.constraint." + constraintType;
        try {
            Object[] args = getConstraintArguments(violation);
            return messageSource.getMessage(genericKey, args, violation.getMessage(), locale);
        } catch (Exception e) {
            logger.debug("No generic validation message found for key: {}, using default", genericKey);
        }
        
        // Fallback to default constraint message (already localized by Bean Validation if configured)
        return violation.getMessage();
    }
    
    private Object[] getConstraintArguments(ConstraintViolation<SmokeTestRequestDto> violation) {
        Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();
        String field = violation.getPropertyPath().toString();
        Object invalidValue = violation.getInvalidValue();
        
        // Common constraint arguments
        List<Object> args = new ArrayList<>();
        args.add(field); // {0} - field name
        
        // Add constraint-specific arguments
        if (attributes.containsKey("max")) {
            args.add(attributes.get("max")); // {1} - max value
        }
        if (attributes.containsKey("min")) {
            args.add(attributes.get("min")); // {1} or {2} - min value
        }
        if (attributes.containsKey("size")) {
            args.add(attributes.get("size")); 
        }
        if (attributes.containsKey("regexp")) {
            args.add(attributes.get("regexp"));
        }
        
        args.add(invalidValue); // Last argument - the invalid value
        
        return args.toArray();
    }
    
    private Locale getLocaleFromExchange(ServerWebExchange exchange) {
        if (exchange == null) {
            return Locale.ENGLISH;
        }
        
        Locale locale = (Locale) exchange.getAttributes().get("LOCALE");
        return locale != null ? locale : Locale.ENGLISH;
    }
}