package com.springSecurity.springSecurity.deserializers;

import com.springSecurity.springSecurity.enums.MusicLibraryProvider;
import com.springSecurity.springSecurity.enums.gd.DemonDifficulty;
import com.springSecurity.springSecurity.enums.gd.Difficulty;
import com.springSecurity.springSecurity.enums.gd.Length;
import com.springSecurity.springSecurity.enums.gd.QualityRating;
import com.springSecurity.springSecurity.models.dto.gd.GDCreatorInfoDTO;
import com.springSecurity.springSecurity.models.dto.gd.GDLevelDTO;
import com.springSecurity.springSecurity.models.dto.gd.GDSongDTO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

import static com.springSecurity.springSecurity.models.dto.gd.GDIndexes.*;
import static com.springSecurity.springSecurity.utils.InternalUtils.*;
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
            Map<Integer, String> data = splitToMap(l, ":");
            list.add(buildLevel(data, structuredCreatorsInfo, structuredSongsInfo));
        }

        return list;
    }

    private GDLevelDTO buildLevel(Map<Integer, String> data, Map<Long, GDCreatorInfoDTO> structuredCreatorsInfo,
                                     Map<Long, GDSongDTO> structuredSongsInfo) {
        requireKeys(data, LEVEL_ID, LEVEL_NAME, LEVEL_CREATOR_ID, LEVEL_DESCRIPTION, LEVEL_DIFFICULTY,
                LEVEL_DEMON_DIFFICULTY, LEVEL_STARS, LEVEL_FEATURED_SCORE, LEVEL_QUALITY_RATING, LEVEL_DOWNLOADS,
                LEVEL_LIKES, LEVEL_LENGTH, LEVEL_COIN_COUNT, LEVEL_COIN_VERIFIED, LEVEL_VERSION, LEVEL_GAME_VERSION,
                LEVEL_OBJECT_COUNT, LEVEL_IS_DEMON, LEVEL_IS_AUTO, LEVEL_ORIGINAL, LEVEL_REQUESTED_STARS,
                LEVEL_SONG_ID, LEVEL_AUDIO_TRACK, LEVEL_TWO_PLAYER);

        final var songId = Optional.ofNullable(data.get(LEVEL_SONG_ID)).map(Long::parseLong).filter(l -> l > 0);

        @SuppressWarnings("SimplifyOptionalCallChains") // IntelliJ bug
        final var song = songId.map(id -> Optional.ofNullable(structuredSongsInfo.get(id)))
                .orElseGet(() -> GDSongDTO.getOfficialSong(Integer.parseInt(data.get(LEVEL_AUDIO_TRACK))));

        final var creatorInfo = structuredCreatorsInfo.get(Long.parseLong(data.get(LEVEL_CREATOR_ID)));

        final var featuredScore = Integer.parseInt(data.get(LEVEL_FEATURED_SCORE));

        return new GDLevelDTO(
                Long.parseLong(data.get(LEVEL_ID)),
                data.get(LEVEL_NAME),
                Long.parseLong(data.get(LEVEL_CREATOR_ID)),
                b64Decode(data.get(LEVEL_DESCRIPTION)),
                Difficulty.parse(data.get(LEVEL_DIFFICULTY)),
                DemonDifficulty.parse(data.get(LEVEL_DEMON_DIFFICULTY)),
                Integer.parseInt(data.get(LEVEL_STARS)),
                featuredScore,
                QualityRating.parse(data.get(LEVEL_QUALITY_RATING), featuredScore > 0),
                Integer.parseInt(data.get(LEVEL_DOWNLOADS)),
                Integer.parseInt(data.get(LEVEL_LIKES)),
                Length.parse(data.get(LEVEL_LENGTH)),
                Integer.parseInt(data.get(LEVEL_COIN_COUNT)),
                data.get(LEVEL_COIN_VERIFIED).equals("1"),
                Integer.parseInt(data.get(LEVEL_VERSION)),
                Integer.parseInt(data.get(LEVEL_GAME_VERSION)),
                Integer.parseInt(data.get(LEVEL_OBJECT_COUNT)),
                data.get(LEVEL_IS_DEMON).equals("1"),
                data.get(LEVEL_IS_AUTO).equals("1"),
                Optional.ofNullable(data.get(LEVEL_ORIGINAL)).map(Long::parseLong).filter(l -> l > 0),
                Integer.parseInt(data.get(LEVEL_REQUESTED_STARS)),
                songId,
                song,
                Optional.ofNullable(creatorInfo).map(GDCreatorInfoDTO::name),
                Optional.ofNullable(creatorInfo).map(GDCreatorInfoDTO::accountId),
                data.get(LEVEL_TWO_PLAYER).equals("1"),
                Objects.equals(data.get(LEVEL_IS_GAUNTLET), "1")
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
            Map<Integer, String> data = splitToMap(songRD, "~\\|~");
            requireKeys(data, SONG_ID, SONG_TITLE, SONG_ARTIST, SONG_SIZE, SONG_URL);
            long songID = Long.parseLong(data.get(SONG_ID));

            result.put(songID, new GDSongDTO(
                    songID,
                    data.get(SONG_TITLE),
                    Long.parseLong(data.get(SONG_ARTIST_ID)),
                    data.get(SONG_ARTIST),
                    Optional.ofNullable(data.get(SONG_SIZE)).filter(not(String::isEmpty)),
                    Optional.ofNullable(data.get(SONG_YOUTUBE_ARTIST)).filter(not(String::isEmpty)),
                    Objects.equals(data.get(SONG_NG_SCOUTED), "1"),
                    Optional.ofNullable(urlDecode(data.get(SONG_URL))).filter(not(String::isEmpty)),
                    MusicLibraryProvider.parse(data.get(SONG_PROVIDER_ID)),
                    toList(data.get(SONG_OTHER_ARTIST_IDS), 0, Long::parseLong, "\\.").orElse(List.of())
            ));
        }

        return result;
    }
}
