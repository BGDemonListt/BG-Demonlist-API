package com.bgdl.bgdl.enums.gd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DemonDifficulty {
    EASY("Easy Demon"),
    MEDIUM("Medium Demon"),
    HARD("Hard Demon"),
    INSANE("Insane Demon"),
    EXTREME("Extreme Demon");

    private final String readableDescription;

    public static DemonDifficulty parse(String str) {
        return switch (str) {
            case "3" -> DemonDifficulty.EASY;
            case "4" -> DemonDifficulty.MEDIUM;
            case "5" -> DemonDifficulty.INSANE;
            case "6" -> DemonDifficulty.EXTREME;
            default -> DemonDifficulty.HARD;
        };
    }

    @Override
    public String toString() {
        return readableDescription;
    }
}