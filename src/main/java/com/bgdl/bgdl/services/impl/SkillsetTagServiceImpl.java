package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.skillsettag.SkillsetTagAlreadyExistsException;
import com.bgdl.bgdl.exceptions.skillsettag.SkillsetTagNotFoundException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.SkillsetTag;
import com.bgdl.bgdl.models.request.SkillsetTagRequest;
import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.SkillsetTagRepository;
import com.bgdl.bgdl.services.SkillsetTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillsetTagServiceImpl implements SkillsetTagService {
    private final SkillsetTagRepository skillsetTagRepository;
    private final DemonRepository demonRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SkillsetTagResponse> getAllTags() {
        return skillsetTagRepository.findAllByDeletedAtIsNullOrderByNameAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SkillsetTagResponse create(SkillsetTagRequest request) {
        String normalizedName = normalizeName(request.getName());
        ensureUniqueName(normalizedName, null);

        SkillsetTag tag = SkillsetTag.builder()
                .name(normalizedName)
                .build();

        return toResponse(skillsetTagRepository.save(tag));
    }

    @Override
    @Transactional
    public SkillsetTagResponse update(UUID id, SkillsetTagRequest request) {
        SkillsetTag tag = getTag(id);
        String normalizedName = normalizeName(request.getName());
        ensureUniqueName(normalizedName, id);

        tag.setName(normalizedName);
        return toResponse(skillsetTagRepository.save(tag));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        SkillsetTag tag = getTag(id);
        List<Demon> demonsWithTag = demonRepository.findAllByDeletedAtIsNullAndSkillsetTags_Id(id);

        for (Demon demon : demonsWithTag) {
            demon.getSkillsetTags().remove(tag);
        }

        if (!demonsWithTag.isEmpty()) {
            demonRepository.saveAll(demonsWithTag);
        }

        skillsetTagRepository.delete(tag);
    }

    private SkillsetTag getTag(UUID id) {
        return skillsetTagRepository.findByDeletedAtIsNullAndId(id)
                .orElseThrow(SkillsetTagNotFoundException::new);
    }

    private void ensureUniqueName(String normalizedName, UUID excludedId) {
        boolean exists = excludedId == null
                ? skillsetTagRepository.findByDeletedAtIsNullAndNameIgnoreCase(normalizedName).isPresent()
                : skillsetTagRepository.findByDeletedAtIsNullAndNameIgnoreCaseAndIdNot(normalizedName, excludedId).isPresent();

        if (exists) {
            throw new SkillsetTagAlreadyExistsException();
        }
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }

    private SkillsetTagResponse toResponse(SkillsetTag tag) {
        SkillsetTagResponse response = new SkillsetTagResponse();
        response.setId(tag.getId());
        response.setName(tag.getName());
        return response;
    }
}
