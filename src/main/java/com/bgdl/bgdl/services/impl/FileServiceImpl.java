package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.file.EmptyFilePathException;
import com.bgdl.bgdl.exceptions.file.FileNotFoundException;
import com.bgdl.bgdl.exceptions.file.FilePathOutsideStaticResourcesException;
import com.bgdl.bgdl.exceptions.file.InvalidFilePathException;
import com.bgdl.bgdl.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    private static final String STATIC_RESOURCE_ROOT = "classpath:static/";

    private final ResourceLoader resourceLoader;

    @Override
    public Resource read(String relativePath) {
        String normalizedPath = normalizePath(relativePath);
        Resource resource = resourceLoader.getResource(STATIC_RESOURCE_ROOT + normalizedPath);

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileNotFoundException();
        }

        return resource;
    }

    private String normalizePath(String relativePath) {
        if (!StringUtils.hasText(relativePath)) {
            throw new EmptyFilePathException();
        }

        try {
            String sanitizedPath = trimLeadingPathSeparators(relativePath.trim());

            if (!StringUtils.hasText(sanitizedPath)) {
                throw new EmptyFilePathException();
            }

            Path normalizedPath = Path.of(sanitizedPath).normalize();

            if (normalizedPath.isAbsolute() || normalizedPath.startsWith("..")) {
                throw new FilePathOutsideStaticResourcesException();
            }

            return normalizedPath.toString().replace('\\', '/');
        } catch (InvalidPathException exception) {
            throw new InvalidFilePathException();
        }
    }

    private String trimLeadingPathSeparators(String path) {
        int index = 0;

        while (index < path.length() && (path.charAt(index) == '/' || path.charAt(index) == '\\')) {
            index++;
        }

        return path.substring(index);
    }
}
