package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DemonCreateException extends BadRequestException {
    public DemonCreateException(boolean isUnique) {
        super(
                isUnique
                        ? "Вече съществува демон със същото ID!"
                        : "Невалидни данни за демона!"
        );
    }
}
