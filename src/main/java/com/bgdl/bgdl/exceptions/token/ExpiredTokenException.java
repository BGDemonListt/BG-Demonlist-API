package com.bgdl.bgdl.exceptions.token;

import com.bgdl.bgdl.exceptions.common.UnauthorizedException;

public class ExpiredTokenException extends UnauthorizedException {
    public ExpiredTokenException() {
        super("Токенът е изтекъл!");
    }
}
