package com.bgdl.bgdl.exceptions.file;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class EmptyFilePathException extends BadRequestException {
    public EmptyFilePathException() {
        super("Пътят към файла не може да бъде празен.");
    }
}
