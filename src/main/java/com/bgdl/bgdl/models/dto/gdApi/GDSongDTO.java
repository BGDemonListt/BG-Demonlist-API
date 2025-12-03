package com.bgdl.bgdl.models.dto.gdApi;

import com.bgdl.bgdl.enums.MusicLibraryProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record GDSongDTO(
        long id,
        String title,
        long artistId,
        String artist,
        Optional<String> size,
        Optional<String> youtubeArtist,
        boolean isArtistScouted,
        Optional<String> downloadUrl,
        MusicLibraryProvider musicLibraryProvider,
        List<Long> otherArtistIds
) {

    private static final Map<Integer, GDSongDTO> OFFICIAL_SONGS = initOfficialSongs();

    private static Map<Integer, GDSongDTO> initOfficialSongs() {
        return Map.ofEntries(
                officialSongEntry(0, "ForeverBound", "Stereo Madness"),
                officialSongEntry(1, "DJVI", "Back On Track"),
                officialSongEntry(2, "Step", "Polargeist"),
                officialSongEntry(3, "DJVI", "Dry Out"),
                officialSongEntry(4, "DJVI", "Base After Base"),
                officialSongEntry(5, "DJVI", "Cant Let Go"),
                officialSongEntry(6, "Waterflame", "Jumper"),
                officialSongEntry(7, "Waterflame", "Time Machine"),
                officialSongEntry(8, "DJVI", "Cycles"),
                officialSongEntry(9, "DJVI", "xStep"),
                officialSongEntry(10, "Waterflame", "Clutterfunk"),
                officialSongEntry(11, "DJ-Nate", "Theory of Everything"),
                officialSongEntry(12, "Waterflame", "Electroman Adventures"),
                officialSongEntry(13, "DJ-Nate", "Clubstep"),
                officialSongEntry(14, "DJ-Nate", "Electrodynamix"),
                officialSongEntry(15, "Waterflame", "Hexagon Force"),
                officialSongEntry(16, "Waterflame", "Blast Processing"),
                officialSongEntry(17, "DJ-Nate", "Theory of Everything 2"),
                officialSongEntry(18, "Waterflame", "Geometrical Dominator"),
                officialSongEntry(19, "F-777", "Deadlocked"),
                officialSongEntry(20, "MDK", "Fingerdash"),
                officialSongEntry(21, "MDK", "Dash"));
    }

    private static Map.Entry<Integer, GDSongDTO> officialSongEntry(int id, String artist, String title) {
        return Map.entry(id, new GDSongDTO(
                id,
                title,
                0L,
                artist,
                Optional.empty(),
                Optional.empty(),
                false,
                Optional.empty(),
                MusicLibraryProvider.OTHER,
                List.of()
        ));
    }

    private static boolean isMusicLibraryId(long id) {
        return id >= 10_000_000;
    }

    public static Optional<GDSongDTO> getOfficialSong(int id) {
        return Optional.ofNullable(OFFICIAL_SONGS.get(id));
    }

    public static GDSongDTO fromNewgrounds(long id, String title, long artistId, String artist, String size,
                                           boolean isArtistScouted, @Nullable String downloadUrl) {
        if (isMusicLibraryId(id)) {
            throw new IllegalArgumentException("id must be < 10_000_000 for a Newgrounds song");
        }
        Objects.requireNonNull(title);
        Objects.requireNonNull(artist);
        Objects.requireNonNull(size);
        return new GDSongDTO(
                id,
                title,
                artistId,
                artist,
                Optional.of(size),
                Optional.empty(),
                isArtistScouted,
                Optional.ofNullable(downloadUrl),
                MusicLibraryProvider.OTHER,
                List.of()
        );
    }

    public static GDSongDTO fromMusicLibrary(long id, String title, long artistId, String artist, String size,
                                             @Nullable String youtubeArtist, MusicLibraryProvider musicLibraryProvider,
                                             List<Long> otherArtistIds) {
        if (!isMusicLibraryId(id)) {
            throw new IllegalArgumentException("id must be >= 10_000_000 for a Music Library song");
        }
        Objects.requireNonNull(title);
        Objects.requireNonNull(artist);
        Objects.requireNonNull(size);
        Objects.requireNonNull(musicLibraryProvider);
        Objects.requireNonNull(otherArtistIds);
        return new GDSongDTO(
                id,
                title,
                artistId,
                artist,
                Optional.of(size),
                Optional.ofNullable(youtubeArtist),
                false,
                Optional.of("CUSTOMURL"),
                musicLibraryProvider,
                otherArtistIds
        );
    }

    public boolean isOfficial() {
        return OFFICIAL_SONGS.containsKey((int) id);
    }

    public boolean isFromNewgrounds() {
        return !OFFICIAL_SONGS.containsKey((int) id) && !isMusicLibraryId(id);
    }

    public boolean isFromMusicLibrary() {
        return !OFFICIAL_SONGS.containsKey((int) id) && isMusicLibraryId(id);
    }

    public boolean isNCS() {
        return musicLibraryProvider == MusicLibraryProvider.NCS;
    }
}