package com.springSecurity.springSecurity.enums.gd;

public enum QualityRating {
    NONE,
    FEATURED,
    EPIC,
    LEGENDARY,
    MYTHIC;

    public static QualityRating parse(String str, boolean defaultFeatured) {
        return switch (str) {
            case "1" -> EPIC;
            case "2" -> LEGENDARY;
            case "3" -> MYTHIC;
            default -> defaultFeatured ? FEATURED : NONE;
        };
    }
}