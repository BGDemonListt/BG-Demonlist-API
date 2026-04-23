package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.UnauthorizedException;

public class InvalidDiscordAuthorizationException extends UnauthorizedException {
    public InvalidDiscordAuthorizationException() {
        super("Discord authorization failed.");
    }
}
