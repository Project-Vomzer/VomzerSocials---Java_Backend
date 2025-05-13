package org.vomzersocials.user.configs;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.vomzersocials.user.dtos.responses.CreatePostResponse;
import org.vomzersocials.user.exceptions.OwnershipException;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CreatePostResponse> handleException(Exception ex) {
        HttpStatus status = determineStatus(ex);
        return ResponseEntity.status(status)
                .body(CreatePostResponse.builder()
                        .errorMessage(ex.getMessage())
                        .build());
    }

    private HttpStatus determineStatus(Exception ex) {
        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof SecurityException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (ex instanceof OwnershipException) {
            return HttpStatus.FORBIDDEN;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}