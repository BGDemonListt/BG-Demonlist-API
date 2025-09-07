package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.ValidationException;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class UserValidationException extends ValidationException {
    public UserValidationException(Set<ConstraintViolation<?>> validationErrors) {
        super(validationErrors);
    }
}
