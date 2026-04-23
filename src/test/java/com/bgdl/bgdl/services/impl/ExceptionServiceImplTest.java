package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.enums.ExceptionSeverity;
import com.bgdl.bgdl.exceptions.common.ApiException;
import com.bgdl.bgdl.models.entity.Exception;
import com.bgdl.bgdl.repositories.ExceptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ExceptionServiceImplTest {

    @Mock
    private ExceptionRepository exceptionRepository;

    @InjectMocks
    private ExceptionServiceImpl exceptionService;

    @Test
    void logApiExceptionSavesInformationalEntryWithoutStackTrace() {
        ApiException apiException = new TestApiException("bad request");

        exceptionService.log(apiException);

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionRepository).save(exceptionCaptor.capture());

        Exception savedException = exceptionCaptor.getValue();
        assertEquals(HttpStatus.BAD_REQUEST.value(), savedException.getStatusCode());
        assertEquals(TestApiException.class.getName(), savedException.getExceptionType());
        assertEquals("bad request", savedException.getExceptionMessage());
        assertEquals(ExceptionSeverity.INFORMATIONAL, savedException.getSeverity());
        assertNull(savedException.getStackTraceString());
        assertNotNull(savedException.getMethodName());
    }

    @Test
    void logRuntimeExceptionSavesCriticalEntryWithTraceMetadata() {
        RuntimeException runtimeException = new RuntimeException("boom");
        runtimeException.setStackTrace(new StackTraceElement[]{
                new StackTraceElement("com.bgdl.bgdl.TestClass", "testMethod", "TestClass.java", 42)
        });

        PrintStream originalErr = System.err;
        ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();

        try {
            System.setErr(new PrintStream(capturedErr));
            exceptionService.log(runtimeException, HttpStatus.INTERNAL_SERVER_ERROR.value());
        } finally {
            System.setErr(originalErr);
        }

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionRepository).save(exceptionCaptor.capture());

        Exception savedException = exceptionCaptor.getValue();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), savedException.getStatusCode());
        assertEquals(RuntimeException.class.getName(), savedException.getExceptionType());
        assertEquals("boom", savedException.getExceptionMessage());
        assertEquals(ExceptionSeverity.CRITICAL, savedException.getSeverity());
        assertEquals("testMethod", savedException.getMethodName());
        assertEquals("com.bgdl.bgdl.TestClass", savedException.getClassName());
        assertEquals(42, savedException.getLineNumber());
        assertNotNull(savedException.getStackTraceString());
    }

    private static final class TestApiException extends ApiException {
        private TestApiException(String message) {
            super(message, HttpStatus.BAD_REQUEST);
        }
    }
}
