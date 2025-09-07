package com.bgdl.bgdl.enums;

import java.util.Objects;

public enum MusicLibraryProvider {
    OTHER,
    NCS;

    public static MusicLibraryProvider parse(String value) {
        return Objects.equals(value, "1") ? NCS : OTHER;
    }
}