package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.file.EmptyFilePathException;
import com.bgdl.bgdl.exceptions.file.FileNotFoundException;
import com.bgdl.bgdl.exceptions.file.FilePathOutsideStaticResourcesException;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileServiceImplTest {

    private final FileServiceImpl fileService = new FileServiceImpl(new DefaultResourceLoader());

    @Test
    void readReturnsStaticResourceForValidPath() throws Exception {
        Resource resource = fileService.read("regions/flags/targovishte.png");

        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertTrue(resource.contentLength() > 0);
        assertEquals("targovishte.png", resource.getFilename());
    }

    @Test
    void readAcceptsLeadingSlashFromCatchAllControllerPath() throws Exception {
        Resource resource = fileService.read("/regions/flags/targovishte.png");

        assertTrue(resource.exists());
        assertEquals("targovishte.png", resource.getFilename());
    }

    @Test
    void readThrowsWhenPathIsBlank() {
        EmptyFilePathException exception = assertThrows(EmptyFilePathException.class, () -> fileService.read(" "));

        assertEquals("Пътят към файла не може да бъде празен.", exception.getMessage());
    }

    @Test
    void readThrowsWhenPathEscapesStaticDirectory() {
        FilePathOutsideStaticResourcesException exception = assertThrows(FilePathOutsideStaticResourcesException.class, () -> fileService.read("../application.yaml"));

        assertEquals("Пътят към файла трябва да остане в директорията със статични ресурси.", exception.getMessage());
    }

    @Test
    void readThrowsWhenFileDoesNotExist() {
        FileNotFoundException exception = assertThrows(FileNotFoundException.class, () -> fileService.read("regions/flags/missing.png"));

        assertEquals("Файлът не е намерен.", exception.getMessage());
    }
}
