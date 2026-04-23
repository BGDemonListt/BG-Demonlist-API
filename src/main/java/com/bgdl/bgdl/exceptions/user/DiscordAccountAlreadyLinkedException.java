package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DiscordAccountAlreadyLinkedException extends BadRequestException {
    public DiscordAccountAlreadyLinkedException() {
        super("Този Discord акаунт вече е свързан с друг потребител.");
    }
}
