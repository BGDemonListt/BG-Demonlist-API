package com.springSecurity.springSecurity.enums.gd;

import com.springSecurity.springSecurity.utils.InternalUtils;

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