package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.deserializers.LevelSearchResponseDeserializer;
import com.bgdl.bgdl.exceptions.common.BadRequestException;
import com.bgdl.bgdl.models.dto.gd.GDLevelDTO;
import com.bgdl.bgdl.services.GdRequestService;
import com.bgdl.bgdl.services.GdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GdServiceImpl implements GdService {
    private final GdRequestService gdRequestService;
    private final LevelSearchResponseDeserializer levelSearchResponseDeserializer;

    @Override
    public GDLevelDTO getGDLevelById(String levelId) {
        String levelData = gdRequestService.getLevelById(levelId);

        if (levelData.equals("-1")) {
            throw new BadRequestException("Invalid level ID");
        }

        return levelSearchResponseDeserializer.apply(levelData).get(0);
    }
}
