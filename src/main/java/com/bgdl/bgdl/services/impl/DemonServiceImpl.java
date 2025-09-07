package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.models.entity.Demon;
import com.bgdl.bgdl.repositories.DemonRepository;
import com.bgdl.bgdl.services.DemonService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Component
@AllArgsConstructor
public class DemonServiceImpl implements DemonService {
    private final ModelMapper modelMapper;
    private final DemonRepository demonRepository;

    @Override
    public List<DemonResponseDTO> getAllDemons() {
        List<Demon> demons = demonRepository.findByDeletedAtIsNullOrderByPositionAsc();
        return demons.stream().map(x -> modelMapper.map(x, DemonResponseDTO.class)).toList();
    }
}
