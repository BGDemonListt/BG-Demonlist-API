package com.bgdl.bgdl.exceptions.file;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class InvalidFilePathException extends BadRequestException {
    public InvalidFilePathException() {
        super("Невалиден път към файл.");
    }
}
