package com.bgdl.bgdl.controllers;

import com.bgdl.bgdl.models.dto.response.DemonResponseDTO;
import com.bgdl.bgdl.services.DemonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/demons")
@RequiredArgsConstructor
public class DemonController {
    private final DemonService demonService;

    @GetMapping
    public List<DemonResponseDTO> getAllDemons() {
        return demonService.getAllDemons();
    }
}
