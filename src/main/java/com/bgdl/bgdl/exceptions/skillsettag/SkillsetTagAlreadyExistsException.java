package com.bgdl.bgdl.exceptions.skillsettag;

import com.bgdl.bgdl.exceptions.common.BadRequestException;

public class SkillsetTagAlreadyExistsException extends BadRequestException {
    public SkillsetTagAlreadyExistsException() {
        super("Вече съществува таг за умения със същото име!");
    }
}
