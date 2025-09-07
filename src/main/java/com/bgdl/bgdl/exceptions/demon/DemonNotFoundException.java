package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class DemonNotFoundException extends NoSuchElementException {
    public DemonNotFoundException() {
        super("The demon is not found!");
    }
}