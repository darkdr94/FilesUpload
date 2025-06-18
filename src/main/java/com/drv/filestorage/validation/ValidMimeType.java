package com.drv.filestorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación personalizada para el content type de los archivos
 */
@Documented
@Constraint(validatedBy = MimeTypeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMimeType {

    String message() default "El tipo MIME no coincide con la extensión del archivo";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}