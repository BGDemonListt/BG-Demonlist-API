package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.UnauthorizedException;

public class InvalidDiscordLinkStateException extends UnauthorizedException {
    public InvalidDiscordLinkStateException() {
        super("Invalid or expired Discord link state.");
    }
}
