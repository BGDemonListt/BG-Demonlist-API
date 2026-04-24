UPDATE players
SET region = 'SOFIA'
WHERE region IN ('SOFIA_CITY', 'SOFIA_PROVINCE');

ALTER TABLE players
    DROP CONSTRAINT IF EXISTS players_region_check;

ALTER TABLE players
    ADD CONSTRAINT players_region_check
    CHECK (
        region IS NULL OR region IN (
            'BLAGOEVGRAD',
            'BURGAS',
            'DOBRICH',
            'GABROVO',
            'HASKOVO',
            'KARDZHALI',
            'KYUSTENDIL',
            'LOVECH',
            'MONTANA',
            'PAZARDZHIK',
            'PERNIK',
            'PLEVEN',
            'PLOVDIV',
            'RAZGRAD',
            'RUSE',
            'SHUMEN',
            'SILISTRA',
            'SLIVEN',
            'SMOLYAN',
            'SOFIA',
            'STARA_ZAGORA',
            'TARGOVISHTE',
            'VARNA',
            'VELIKO_TARNOVO',
            'VIDIN',
            'VRATSA',
            'YAMBOL'
        )
    );
