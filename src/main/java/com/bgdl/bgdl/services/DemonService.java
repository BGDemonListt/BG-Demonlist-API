package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.models.entity.Demon;

import java.util.List;
import java.util.UUID;

public interface DemonService {
    List<DemonResponse> getAllDemons();

    Demon getById(UUID id);

    DemonResponse getDemonByLevelId(long levelId);

    DemonResponse createDemon(DemonRequest demonRequest);

    DemonResponse update(long levelId, DemonRequest demonRequest);

    void delete(UUID id);
}
