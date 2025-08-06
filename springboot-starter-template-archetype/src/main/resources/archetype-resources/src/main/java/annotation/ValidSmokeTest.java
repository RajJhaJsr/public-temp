#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.annotation;

import ${package}.validator.SmokeTestValidator2;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SmokeTestValidator2.class)
public @interface ValidSmokeTest {
    String message() default "smoke.test.invalid";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

