ALTER TABLE IF EXISTS players
    ADD COLUMN IF NOT EXISTS region VARCHAR(64);

DO $$
BEGIN
    IF to_regclass('public.players') IS NOT NULL
       AND NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'players_region_check'
       ) THEN
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
                    'SOFIA_CITY',
                    'SOFIA_PROVINCE',
                    'STARA_ZAGORA',
                    'TARGOVISHTE',
                    'VARNA',
                    'VELIKO_TARNOVO',
                    'VIDIN',
                    'VRATSA',
                    'YAMBOL'
                )
            );
    END IF;
END $$;
