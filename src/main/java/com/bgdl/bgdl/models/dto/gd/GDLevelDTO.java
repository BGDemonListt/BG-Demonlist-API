package com.bgdl.bgdl.models.dto.gd;

import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.enums.gd.Difficulty;
import com.bgdl.bgdl.enums.gd.Length;
import com.bgdl.bgdl.enums.gd.QualityRating;

import java.util.Optional;

public record GDLevelDTO(
        long id,
        String name,
        long creatorPlayerId,
        String description,
        Difficulty votedDifficulty,
        DemonDifficulty demonDifficulty,
        int rewards,
        int featuredScore,
        QualityRating qualityRating,
        int downloads,
        int likes,
        Length length,
        int coinCount,
        boolean hasCoinsVerified,
        int levelVersion,
        int gameVersion,
        int objectCount,
        boolean isDemon,
        boolean isAuto,
        Optional<Long> originalLevelId,
        int requestedStars,
        Optional<Long> songId,
        Optional<GDSongDTO> song,
        Optional<String> creatorName,
        Optional<Long> creatorAccountId,
        boolean isTwoPlayer,
        boolean isGauntlet
) {
    public Difficulty difficulty() {
        if (isDemon) {
            return Difficulty.DEMON;
        }
        if (isAuto) {
            return Difficulty.AUTO;
        }
        return votedDifficulty;
    }

    public boolean isPlatformer() {
        return length == Length.PLATFORMER;
    }
}