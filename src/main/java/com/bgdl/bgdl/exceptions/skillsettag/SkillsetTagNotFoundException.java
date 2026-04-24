package com.bgdl.bgdl.exceptions.skillsettag;

import com.bgdl.bgdl.exceptions.common.NoSuchElementException;

public class SkillsetTagNotFoundException extends NoSuchElementException {
    public SkillsetTagNotFoundException() {
        super("Тагът за умения не е намерен!");
    }
}
