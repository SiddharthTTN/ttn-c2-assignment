package com.ttn.support.rag;

import com.ttn.support.web.error.ModelUnavailableException;
import org.springframework.web.client.RestClientException;

public final class ModelInfrastructureExceptionMapper {

    private ModelInfrastructureExceptionMapper() {}

    public static RuntimeException toModelUnavailable(String operation, RuntimeException cause) {
        if (cause instanceof ModelUnavailableException) {
            return cause;
        }
        if (isInfrastructureFailure(cause)) {
            return new ModelUnavailableException("Model infrastructure is unavailable during " + operation, cause);
        }
        return cause;
    }

    private static boolean isInfrastructureFailure(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof RestClientException) {
                return true;
            }
            String name = current.getClass().getName();
            if (name.startsWith("org.springframework.ai.") && name.contains("Exception")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
