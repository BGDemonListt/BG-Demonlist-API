package com.bgdl.bgdl.enums.gd;

import com.bgdl.bgdl.utils.InternalUtils;

public enum Length {
    TINY,
    SHORT,
    MEDIUM,
    LONG,
    XL,
    PLATFORMER;

    public static Length parse(String str) {
        return InternalUtils.parseIndex(str, Length.values());
    }
}