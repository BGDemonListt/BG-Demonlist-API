package com.bgdl.bgdl.deserializers;

import com.bgdl.bgdl.enums.MusicLibraryProvider;
import com.bgdl.bgdl.enums.gd.DemonDifficulty;
import com.bgdl.bgdl.enums.gd.Difficulty;
import com.bgdl.bgdl.enums.gd.Length;
import com.bgdl.bgdl.enums.gd.QualityRating;
import com.bgdl.bgdl.models.dto.gd.GDCreatorInfoDTO;
import com.bgdl.bgdl.models.dto.gd.GDIndexes;
import com.bgdl.bgdl.models.dto.gd.GDLevelDTO;
import com.bgdl.bgdl.models.dto.gd.GDSongDTO;
import com.bgdl.bgdl.utils.InternalUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static java.util.function.Predicate.not;

@Component
public class LevelSearchResponseDeserializer implements Function<String, List<GDLevelDTO>> {

    @Override
    public List<GDLevelDTO> apply(String response) {
        String[] splitData = response.split("#");
        String levels = splitData[0];
        String creators = splitData[1];
        String songs = splitData[2];

        Map<Long, GDCreatorInfoDTO> structuredCreatorsInfo = structureCreatorsInfo(creators);
        Map<Long, GDSongDTO> structuredSongsInfo = structureSongsInfo(songs);

        ArrayList<GDLevelDTO> list = new ArrayList<>();
        String[] levelArray = levels.split("\\|");
        for (String l : levelArray) {
            Map<Integer, String> data = InternalUtils.splitToMap(l, ":");
            list.add(buildLevel(data, structuredCreatorsInfo, structuredSongsInfo));
        }

        return list;
    }

    private GDLevelDTO buildLevel(Map<Integer, String> data, Map<Long, GDCreatorInfoDTO> structuredCreatorsInfo,
                                  Map<Long, GDSongDTO> structuredSongsInfo) {
        InternalUtils.requireKeys(data, GDIndexes.LEVEL_ID, GDIndexes.LEVEL_NAME, GDIndexes.LEVEL_CREATOR_ID, GDIndexes.LEVEL_DESCRIPTION, GDIndexes.LEVEL_DIFFICULTY,
                GDIndexes.LEVEL_DEMON_DIFFICULTY, GDIndexes.LEVEL_STARS, GDIndexes.LEVEL_FEATURED_SCORE, GDIndexes.LEVEL_QUALITY_RATING, GDIndexes.LEVEL_DOWNLOADS,
                GDIndexes.LEVEL_LIKES, GDIndexes.LEVEL_LENGTH, GDIndexes.LEVEL_COIN_COUNT, GDIndexes.LEVEL_COIN_VERIFIED, GDIndexes.LEVEL_VERSION, GDIndexes.LEVEL_GAME_VERSION,
                GDIndexes.LEVEL_OBJECT_COUNT, GDIndexes.LEVEL_IS_DEMON, GDIndexes.LEVEL_IS_AUTO, GDIndexes.LEVEL_ORIGINAL, GDIndexes.LEVEL_REQUESTED_STARS,
                GDIndexes.LEVEL_SONG_ID, GDIndexes.LEVEL_AUDIO_TRACK, GDIndexes.LEVEL_TWO_PLAYER);

        final var songId = Optional.ofNullable(data.get(GDIndexes.LEVEL_SONG_ID)).map(Long::parseLong).filter(l -> l > 0);

        @SuppressWarnings("SimplifyOptionalCallChains") // IntelliJ bug
        final var song = songId.map(id -> Optional.ofNullable(structuredSongsInfo.get(id)))
                .orElseGet(() -> GDSongDTO.getOfficialSong(Integer.parseInt(data.get(GDIndexes.LEVEL_AUDIO_TRACK))));

        final var creatorInfo = structuredCreatorsInfo.get(Long.parseLong(data.get(GDIndexes.LEVEL_CREATOR_ID)));

        final var featuredScore = Integer.parseInt(data.get(GDIndexes.LEVEL_FEATURED_SCORE));

        return new GDLevelDTO(
                Long.parseLong(data.get(GDIndexes.LEVEL_ID)),
                data.get(GDIndexes.LEVEL_NAME),
                Long.parseLong(data.get(GDIndexes.LEVEL_CREATOR_ID)),
                InternalUtils.b64Decode(data.get(GDIndexes.LEVEL_DESCRIPTION)),
                Difficulty.parse(data.get(GDIndexes.LEVEL_DIFFICULTY)),
                DemonDifficulty.parse(data.get(GDIndexes.LEVEL_DEMON_DIFFICULTY)),
                Integer.parseInt(data.get(GDIndexes.LEVEL_STARS)),
                featuredScore,
                QualityRating.parse(data.get(GDIndexes.LEVEL_QUALITY_RATING), featuredScore > 0),
                Integer.parseInt(data.get(GDIndexes.LEVEL_DOWNLOADS)),
                Integer.parseInt(data.get(GDIndexes.LEVEL_LIKES)),
                Length.parse(data.get(GDIndexes.LEVEL_LENGTH)),
                Integer.parseInt(data.get(GDIndexes.LEVEL_COIN_COUNT)),
                data.get(GDIndexes.LEVEL_COIN_VERIFIED).equals("1"),
                Integer.parseInt(data.get(GDIndexes.LEVEL_VERSION)),
                Integer.parseInt(data.get(GDIndexes.LEVEL_GAME_VERSION)),
                Integer.parseInt(data.get(GDIndexes.LEVEL_OBJECT_COUNT)),
                data.get(GDIndexes.LEVEL_IS_DEMON).equals("1"),
                data.get(GDIndexes.LEVEL_IS_AUTO).equals("1"),
                Optional.ofNullable(data.get(GDIndexes.LEVEL_ORIGINAL)).map(Long::parseLong).filter(l -> l > 0),
                Integer.parseInt(data.get(GDIndexes.LEVEL_REQUESTED_STARS)),
                songId,
                song,
                Optional.ofNullable(creatorInfo).map(GDCreatorInfoDTO::name),
                Optional.ofNullable(creatorInfo).map(GDCreatorInfoDTO::accountId),
                data.get(GDIndexes.LEVEL_TWO_PLAYER).equals("1"),
                Objects.equals(data.get(GDIndexes.LEVEL_IS_GAUNTLET), "1")
        );
    }

    private Map<Long, GDCreatorInfoDTO> structureCreatorsInfo(String creatorsInfoRD) {
        if (!creatorsInfoRD.matches("\\d+:[^:|]+:\\d+(\\|\\d+:[^:|]+:\\d+)*")) {
            return Collections.emptyMap();
        }

        String[] arrayCreatorsRD = creatorsInfoRD.split("\\|");
        Map<Long, GDCreatorInfoDTO> structuredCreatorsInfo = new HashMap<>();

        for (String creatorRD : arrayCreatorsRD) {
            structuredCreatorsInfo.put(Long.parseLong(creatorRD.split(":")[0]),
                    new GDCreatorInfoDTO(creatorRD.split(":")[1], Long.parseLong(creatorRD.split(":")[2])));
        }

        return structuredCreatorsInfo;
    }

    private Map<Long, GDSongDTO> structureSongsInfo(String songsInfoRD) {
        if (songsInfoRD.isEmpty())
            return Collections.emptyMap();

        String[] arraySongsRD = songsInfoRD.split("~:~");
        Map<Long, GDSongDTO> result = new HashMap<>();

        for (String songRD : arraySongsRD) {
            Map<Integer, String> data = InternalUtils.splitToMap(songRD, "~\\|~");
            InternalUtils.requireKeys(data, GDIndexes.SONG_ID, GDIndexes.SONG_TITLE, GDIndexes.SONG_ARTIST, GDIndexes.SONG_SIZE, GDIndexes.SONG_URL);
            long songID = Long.parseLong(data.get(GDIndexes.SONG_ID));

            result.put(songID, new GDSongDTO(
                    songID,
                    data.get(GDIndexes.SONG_TITLE),
                    Long.parseLong(data.get(GDIndexes.SONG_ARTIST_ID)),
                    data.get(GDIndexes.SONG_ARTIST),
                    Optional.ofNullable(data.get(GDIndexes.SONG_SIZE)).filter(not(String::isEmpty)),
                    Optional.ofNullable(data.get(GDIndexes.SONG_YOUTUBE_ARTIST)).filter(not(String::isEmpty)),
                    Objects.equals(data.get(GDIndexes.SONG_NG_SCOUTED), "1"),
                    Optional.ofNullable(InternalUtils.urlDecode(data.get(GDIndexes.SONG_URL))).filter(not(String::isEmpty)),
                    MusicLibraryProvider.parse(data.get(GDIndexes.SONG_PROVIDER_ID)),
                    InternalUtils.toList(data.get(GDIndexes.SONG_OTHER_ARTIST_IDS), 0, Long::parseLong, "\\.").orElse(List.of())
            ));
        }

        return result;
    }
}
