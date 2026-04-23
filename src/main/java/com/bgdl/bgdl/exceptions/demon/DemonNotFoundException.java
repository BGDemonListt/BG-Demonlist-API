package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class DemonNotFoundException extends NoSuchElementException {
    public DemonNotFoundException() {
        super("Демонът не е намерен!");
    }
}
