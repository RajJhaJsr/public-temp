#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.validator;

import ${package}.annotation.ValidSmokeTest;
import ${package}.dto.SmokeTestRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class SmokeTestValidator2 implements ConstraintValidator<ValidSmokeTest, SmokeTestRequestDto> {
    
    @Override
    public boolean isValid(SmokeTestRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }
        
        boolean isValid = true;

        if (dto.name() != null && dto.name().trim().isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("smoke.test.name.empty")
                    .addPropertyNode("name")
                    .addConstraintViolation();
            isValid = false;
        }
        
        return isValid;
    }
}