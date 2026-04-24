package com.bgdl.bgdl.enums;

import java.util.Arrays;
import java.util.List;

public enum BulgarianRegion {
    BLAGOEVGRAD("Blagoevgrad", "/regions/flags/blagoevgrad.svg"),
    BURGAS("Burgas", "/regions/flags/burgas.svg"),
    DOBRICH("Dobrich", "/regions/flags/dobrich.svg"),
    GABROVO("Gabrovo", "/regions/flags/gabrovo.svg"),
    HASKOVO("Haskovo", "/regions/flags/haskovo.svg"),
    KARDZHALI("Kardzhali", "/regions/flags/kardzhali.svg"),
    KYUSTENDIL("Kyustendil", "/regions/flags/kyustendil.svg"),
    LOVECH("Lovech", "/regions/flags/lovech.svg"),
    MONTANA("Montana", "/regions/flags/montana.svg"),
    PAZARDZHIK("Pazardzhik", "/regions/flags/pazardzhik.svg"),
    PERNIK("Pernik", "/regions/flags/pernik.svg"),
    PLEVEN("Pleven", "/regions/flags/pleven.svg"),
    PLOVDIV("Plovdiv", "/regions/flags/plovdiv.svg"),
    RAZGRAD("Razgrad", "/regions/flags/razgrad.svg"),
    RUSE("Ruse", "/regions/flags/ruse.svg"),
    SHUMEN("Shumen", "/regions/flags/shumen.svg"),
    SILISTRA("Silistra", "/regions/flags/silistra.svg"),
    SLIVEN("Sliven", "/regions/flags/sliven.svg"),
    SMOLYAN("Smolyan", "/regions/flags/smolyan.svg"),
    SOFIA_CITY("Sofia City", "/regions/flags/sofia-city.svg"),
    SOFIA_PROVINCE("Sofia Province", "/regions/flags/sofia-province.svg"),
    STARA_ZAGORA("Stara Zagora", "/regions/flags/stara-zagora.svg"),
    TARGOVISHTE("Targovishte", "/regions/flags/targovishte.svg"),
    VARNA("Varna", "/regions/flags/varna.svg"),
    VELIKO_TARNOVO("Veliko Tarnovo", "/regions/flags/veliko-tarnovo.svg"),
    VIDIN("Vidin", "/regions/flags/vidin.svg"),
    VRATSA("Vratsa", "/regions/flags/vratsa.svg"),
    YAMBOL("Yambol", "/regions/flags/yambol.svg");

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
