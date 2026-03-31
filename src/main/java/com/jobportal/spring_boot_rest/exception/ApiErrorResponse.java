package com.jobportal.spring_boot_rest.exception;

import java.time.Instant;

public record ApiErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
