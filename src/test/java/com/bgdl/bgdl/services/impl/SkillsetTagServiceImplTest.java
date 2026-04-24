package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.skillsettag.SkillsetTagAlreadyExistsException;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.models.entity.SkillsetTag;
import com.bgdl.bgdl.models.request.SkillsetTagRequest;
import com.bgdl.bgdl.models.response.SkillsetTagResponse;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.repositories.SkillsetTagRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SkillsetTagServiceImplTest {

    @Mock
    private SkillsetTagRepository skillsetTagRepository;

    @Mock
    private DemonRepository demonRepository;

    @InjectMocks
    private SkillsetTagServiceImpl skillsetTagService;

    @Test
    void createNormalizesWhitespaceAndReturnsSavedTag() {
        SkillsetTagRequest request = new SkillsetTagRequest();
        request.setName("  Wave   Control  ");

        when(skillsetTagRepository.findByDeletedAtIsNullAndNameIgnoreCase("Wave Control")).thenReturn(Optional.empty());
        when(skillsetTagRepository.save(any(SkillsetTag.class))).thenAnswer(invocation -> {
            SkillsetTag tag = invocation.getArgument(0);
            tag.setId(UUID.randomUUID());
            return tag;
        });

        SkillsetTagResponse response = skillsetTagService.create(request);

        assertEquals("Wave Control", response.getName());
    }

    @Test
    void createRejectsDuplicateNameIgnoringCase() {
        SkillsetTagRequest request = new SkillsetTagRequest();
        request.setName("wave");

        when(skillsetTagRepository.findByDeletedAtIsNullAndNameIgnoreCase("wave"))
                .thenReturn(Optional.of(skillsetTag("Wave")));

        assertThrows(SkillsetTagAlreadyExistsException.class, () -> skillsetTagService.create(request));
        verify(skillsetTagRepository, never()).save(any(SkillsetTag.class));
    }

    @Test
    void deleteRemovesTagFromAssignedDemonsBeforeDeletingTag() {
        SkillsetTag tag = skillsetTag("Timing");
        Demon demon = new Demon();
        demon.setId(UUID.randomUUID());
        demon.setSkillsetTags(new LinkedHashSet<>(List.of(tag)));

        when(skillsetTagRepository.findByDeletedAtIsNullAndId(tag.getId())).thenReturn(Optional.of(tag));
        when(demonRepository.findAllByDeletedAtIsNullAndSkillsetTags_Id(tag.getId())).thenReturn(List.of(demon));

        skillsetTagService.delete(tag.getId());

        assertEquals(0, demon.getSkillsetTags().size());
        verify(demonRepository).saveAll(List.of(demon));
        verify(skillsetTagRepository).delete(tag);
    }

    private SkillsetTag skillsetTag(String name) {
        SkillsetTag tag = new SkillsetTag();
        tag.setId(UUID.randomUUID());
        tag.setName(name);
        return tag;
    }
}
