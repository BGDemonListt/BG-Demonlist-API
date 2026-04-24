package com.bgdl.bgdl.services;

import org.springframework.core.io.Resource;

public interface FileService {
    Resource read(String relativePath);
}
