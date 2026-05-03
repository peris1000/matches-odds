package com.zimono.sports_odds.config;

import com.zimono.sports_odds.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalApiExceptionHandlerTest {

    private final GlobalApiExceptionHandler handler = new GlobalApiExceptionHandler();
    private final WebRequest request = mock(WebRequest.class);

    @BeforeEach
    void setUp() {
        when(request.getDescription(false)).thenReturn("uri=/test/path");
    }

    @Test
    void handleResourceNotFoundException_shouldReturnNotFoundStatus() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        
        ResponseEntity<Object> response = handler.handleResourceNotFoundException(ex, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body.get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void handleIllegalArgumentException_shouldReturnConflict() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid arg");
        
        ResponseEntity<Object> response = handler.handleIllegalArgumentException(ex, request);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
