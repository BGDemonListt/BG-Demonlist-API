package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DemonCreateException extends BadRequestException {
    public DemonCreateException(boolean isUnique) {
        super(
                isUnique
                        ? "Demon with the same id already exists!"
                        : "Invalid dmeon data!"
        );
    }
}
