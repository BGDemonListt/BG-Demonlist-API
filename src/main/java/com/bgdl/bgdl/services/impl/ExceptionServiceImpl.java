package com.bgdl.bgdl.services.impl;

import com.bgdl.bgdl.exceptions.common.ApiException;
import com.bgdl.bgdl.models.entity.Exception;
import com.bgdl.bgdl.repositories.ExceptionRepository;
import com.bgdl.bgdl.services.ExceptionService;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Component
@AllArgsConstructor
public class ExceptionServiceImpl implements ExceptionService {
    private final ExceptionRepository exceptionRepository;

    @Override
    @Async
    public void log(ApiException apiException) {
        Exception exception = Exception.mapFromApiException(apiException);
        exceptionRepository.save(exception);
    }

    @Override
    @Async
    public void log(RuntimeException runtimeException, int statusCode) {
        runtimeException.printStackTrace();
        Exception exception = Exception.mapFromRuntimeException(runtimeException, statusCode);
        exceptionRepository.save(exception);
    }
}
