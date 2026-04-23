package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DemonInvalidPositionException extends BadRequestException {
    public DemonInvalidPositionException() {
        super("Позицията трябва да бъде по-голяма от 0!");
    }
}
