package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DemonInvalidPositionException extends BadRequestException {
    public DemonInvalidPositionException() {
        super("Position must be greater than 0!");
    }
}