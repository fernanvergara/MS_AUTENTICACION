package co.com.sti.api.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeValid {

    /**
     * El minimo de eda requerida, por defecto es 18.
     * @return La edad minima.
     */
    int value() default 18;

    /**
     * El mensaje de error.
     * @return mensaje de error.
     */
    String message() default "La fecha de nacimiento debe tener al menos 18 años.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
