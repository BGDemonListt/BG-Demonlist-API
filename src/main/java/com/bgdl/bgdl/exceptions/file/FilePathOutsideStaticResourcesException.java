package com.bgdl.bgdl.exceptions.file;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class FilePathOutsideStaticResourcesException extends BadRequestException {
    public FilePathOutsideStaticResourcesException() {
        super("Пътят към файла трябва да остане в директорията със статични ресурси.");
    }
}
