package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import com.bgdl.bgdl.models.request.SkillsetTagRequest;

import java.util.List;
import java.util.UUID;

public interface SkillsetTagService {
    List<SkillsetTagResponse> getAllTags();

    SkillsetTagResponse create(SkillsetTagRequest request);

    SkillsetTagResponse update(UUID id, SkillsetTagRequest request);

    void delete(UUID id);
}
