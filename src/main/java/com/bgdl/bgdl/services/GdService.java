package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.response.gdApi.GDLevelResponse;

public interface GdService {
    GDLevelResponse getGDLevelById(String levelId);
}
