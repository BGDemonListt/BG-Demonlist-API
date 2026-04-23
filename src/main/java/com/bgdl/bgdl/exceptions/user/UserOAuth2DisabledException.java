package com.bgdl.bgdl.exceptions.user;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class UserOAuth2DisabledException extends BadRequestException {
    public UserOAuth2DisabledException() {
        super("Акаунта който се опитвате да достъпите не е активен. Моля свържете се с нас за помощ.");
    }
}
