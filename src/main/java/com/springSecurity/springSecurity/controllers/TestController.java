package com.springSecurity.springSecurity.controllers;

import com.springSecurity.springSecurity.deserializers.LevelSearchResponseDeserializer;
import com.springSecurity.springSecurity.exceptions.common.BadRequestException;
import com.springSecurity.springSecurity.models.dto.gd.GDLevelDTO;
import com.springSecurity.springSecurity.services.GdRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestController {
    private final GdRequestService gdRequestService;
    private final LevelSearchResponseDeserializer levelSearchResponseDeserializer;

    @GetMapping("/levels/{levelId}")
    public GDLevelDTO getLevels(@PathVariable String levelId) {
        String levelData = gdRequestService.getLevelById(levelId);

        if (levelData.equals("-1")) {
            throw new BadRequestException("Invalid level ID");
        }

        return levelSearchResponseDeserializer.apply(levelData).get(0);
    }
}
