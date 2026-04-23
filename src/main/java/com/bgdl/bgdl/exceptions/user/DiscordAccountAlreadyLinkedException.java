package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DiscordAccountAlreadyLinkedException extends BadRequestException {
    public DiscordAccountAlreadyLinkedException() {
        super("This Discord account is already linked to another user.");
    }
}
