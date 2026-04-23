package com.bgdl.bgdl.exceptions.player;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class PlayerNotFoundException extends NoSuchElementException {
    public PlayerNotFoundException() {
        super("Играчът не е намерен!");
    }
}
