package com.bgdl.bgdl.services;

import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;

import java.util.List;

public interface DemonService {
    List<DemonResponseDTO> getAllDemons();
}
