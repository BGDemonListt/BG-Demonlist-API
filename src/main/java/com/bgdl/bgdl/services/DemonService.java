package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.request.DemonRequest;
import com.bgdl.bgdl.models.response.PageResponse;
import com.bgdl.bgdl.models.response.DemonSummaryResponse;
import com.bgdl.bgdl.models.response.DemonResponse;
import com.bgdl.bgdl.models.entity.Demon;

import java.util.Set;
import java.util.UUID;

public interface DemonService {
    PageResponse<DemonSummaryResponse> getAllDemons(String nameFilter, Set<UUID> skillsetTagIds, int page);

    Demon getById(UUID id);

    DemonResponse getDemonByLevelId(long levelId);

    DemonResponse createDemon(DemonRequest demonRequest);

    DemonResponse update(long levelId, DemonRequest demonRequest);

    void delete(UUID id);
}
