package com.springSecurity.springSecurity.enums;

import java.util.Objects;

public enum MusicLibraryProvider {
    OTHER,
    NCS;

    public static MusicLibraryProvider parse(String value) {
        return Objects.equals(value, "1") ? NCS : OTHER;
    }
}