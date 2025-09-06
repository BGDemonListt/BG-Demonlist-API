package com.springSecurity.springSecurity.enums.gd;

import lombok.Getter;

@Getter
public enum Difficulty {
    NA(-1),
    AUTO(-2),
    EASY(1),
    NORMAL(2),
    HARD(3),
    HARDER(4),
    INSANE(5),
    DEMON(-3);

    private final int value;

    Difficulty(int value) {
        this.value = value;
    }

    public static Difficulty parse(String str) {
        return switch (str) {
            case "10" -> Difficulty.EASY;
            case "20" -> Difficulty.NORMAL;
            case "30" -> Difficulty.HARD;
            case "40" -> Difficulty.HARDER;
            case "50" -> Difficulty.INSANE;
            default -> Difficulty.NA;
        };
    }

}