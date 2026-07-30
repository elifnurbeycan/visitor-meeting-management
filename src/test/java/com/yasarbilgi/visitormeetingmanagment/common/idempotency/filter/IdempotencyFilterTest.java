package com.yasarbilgi.visitormeetingmanagment.common.idempotency.filter;

import com.yasarbilgi.visitormeetingmanagment.common.idempotency.repository.IdempotencyKeyRepository;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class IdempotencyFilterTest {

    private IdempotencyKeyRepository idempotencyKeyRepository;
    private FilterChain filterChain;
    private IdempotencyFilter idempotencyFilter;

    @BeforeEach
    void setUp() {
        idempotencyKeyRepository = mock(IdempotencyKeyRepository.class);
        filterChain = mock(FilterChain.class);
        idempotencyFilter = new IdempotencyFilter(idempotencyKeyRepository);
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsNotValidUuid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/reservations"
        );
        request.addHeader("Idempotency-Key", "invalid-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        idempotencyFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .contains("Idempotency-Key must be a valid UUID");

        verifyNoInteractions(idempotencyKeyRepository);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldContinueWhenIdempotencyKeyIsValidUuid()
            throws Exception {

        String validUuid = UUID.randomUUID().toString();

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/reservations"
        );
        request.addHeader("Idempotency-Key", validUuid);

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(idempotencyKeyRepository.findByIdempotencyKey(validUuid))
                .thenReturn(Optional.empty());

        idempotencyFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(
                eq(request),
                any()
        );
    }

    @Test
    void shouldRejectNonCanonicalUuid()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/reservations"
        );
        request.addHeader("Idempotency-Key", "1-1-1-1-1");

        MockHttpServletResponse response = new MockHttpServletResponse();

        idempotencyFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(400);

        verifyNoInteractions(idempotencyKeyRepository);
        verifyNoInteractions(filterChain);
    }

    @Test
    void shouldSkipUuidValidationForDifferentEndpoint()
            throws Exception {

        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/v1/login"
        );
        request.addHeader("Idempotency-Key", "invalid-key");

        MockHttpServletResponse response = new MockHttpServletResponse();

        idempotencyFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(idempotencyKeyRepository);
    }
}