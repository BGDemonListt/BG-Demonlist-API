package com.bgdl.bgdl.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestController {
    @GetMapping("/{id}")
    public String getTest(@PathVariable String id) {
        return "For dev test purposes";
    }
}
