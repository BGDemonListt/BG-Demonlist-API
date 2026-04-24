package com.bgdl.bgdl.exceptions.file;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class FileNotFoundException extends NoSuchElementException {
    public FileNotFoundException() {
        super("Файлът не е намерен.");
    }
}
