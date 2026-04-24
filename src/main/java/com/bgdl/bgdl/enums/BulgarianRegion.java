package com.bgdl.bgdl.enums;

import java.util.Arrays;
import java.util.List;

public enum BulgarianRegion {
    BLAGOEVGRAD("Blagoevgrad", "/regions/flags/blagoevgrad.png"),
    BURGAS("Burgas", "/regions/flags/burgas.png"),
    DOBRICH("Dobrich", "/regions/flags/dobrich.png"),
    GABROVO("Gabrovo", "/regions/flags/gabrovo.png"),
    HASKOVO("Haskovo", "/regions/flags/haskovo.png"),
    KARDZHALI("Kardzhali", "/regions/flags/kardzhali.png"),
    KYUSTENDIL("Kyustendil", "/regions/flags/kyustendil.png"),
    LOVECH("Lovech", "/regions/flags/lovech.png"),
    MONTANA("Montana", "/regions/flags/montana.png"),
    PAZARDZHIK("Pazardzhik", "/regions/flags/pazardzhik.png"),
    PERNIK("Pernik", "/regions/flags/pernik.png"),
    PLEVEN("Pleven", "/regions/flags/pleven.png"),
    PLOVDIV("Plovdiv", "/regions/flags/plovdiv.png"),
    RAZGRAD("Razgrad", "/regions/flags/razgrad.png"),
    RUSE("Ruse", "/regions/flags/ruse.png"),
    SHUMEN("Shumen", "/regions/flags/shumen.png"),
    SILISTRA("Silistra", "/regions/flags/silistra.png"),
    SLIVEN("Sliven", "/regions/flags/sliven.png"),
    SMOLYAN("Smolyan", "/regions/flags/smolyan.png"),
    SOFIA("Sofia", "/regions/flags/sofia.png"),
    STARA_ZAGORA("Stara Zagora", "/regions/flags/stara_zagora.png"),
    TARGOVISHTE("Targovishte", "/regions/flags/targovishte.png"),
    VARNA("Varna", "/regions/flags/varna.png"),
    VELIKO_TARNOVO("Veliko Tarnovo", "/regions/flags/veliko_tarnovo.png"),
    VIDIN("Vidin", "/regions/flags/vidin.png"),
    VRATSA("Vratsa", "/regions/flags/vratza.png"),
    YAMBOL("Yambol", "/regions/flags/yambol.png");

    private final String displayName;
    private final String flagPath;

    BulgarianRegion(String displayName, String flagPath) {
        this.displayName = displayName;
        this.flagPath = flagPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getFlagPath() {
        return flagPath;
    }

    public static List<BulgarianRegion> orderedValues() {
        return Arrays.asList(values());
    }
}
