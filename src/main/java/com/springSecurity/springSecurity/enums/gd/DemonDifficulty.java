package com.springSecurity.springSecurity.enums.gd;

public enum DemonDifficulty {
    EASY,
    MEDIUM,
    HARD,
    INSANE,
    EXTREME;

    public static DemonDifficulty parse(String str) {
        return switch (str) {
            case "3" -> DemonDifficulty.EASY;
            case "4" -> DemonDifficulty.MEDIUM;
            case "5" -> DemonDifficulty.INSANE;
            case "6" -> DemonDifficulty.EXTREME;
            default -> DemonDifficulty.HARD;
        };
    }
}