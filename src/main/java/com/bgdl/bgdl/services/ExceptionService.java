package com.bgdl.bgdl.services;

import com.bgdl.bgdl.exceptions.common.ApiException;

public interface ExceptionService {

    void log(ApiException runtimeException);

    void log(RuntimeException runtimeException, int statusCode);
}
