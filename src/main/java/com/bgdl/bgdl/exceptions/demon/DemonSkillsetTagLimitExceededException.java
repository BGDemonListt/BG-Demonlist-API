package com.bgdl.bgdl.exceptions.demon;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class DemonSkillsetTagLimitExceededException extends BadRequestException {
    public DemonSkillsetTagLimitExceededException() {
        super("Един демон може да има най-много 4 тага за умения!");
    }
}
