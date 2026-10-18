package com.pulse.pass.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @Test
    @DisplayName("NFR-SEC-006: generates an id when the request has none and exposes it during the chain")
    void noHeader_generatesIdVisibleInMdcAndResponse() throws Exception {
        // ARRANGE
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> seenInChain = new AtomicReference<>();
        FilterChain chain = (req, res) -> seenInChain.set(MDC.get(RequestIdFilter.MDC_KEY));

        // ACT
        filter.doFilter(request, response, chain);

        // ASSERT
        assertThat(seenInChain.get()).isNotBlank();
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo(seenInChain.get());
    }

    @Test
    @DisplayName("NFR-SEC-006: keeps a well-formed incoming id")
    void safeHeader_isKept() throws Exception {
        // ARRANGE
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "req-123.abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // ACT
        filter.doFilter(request, response, (req, res) -> { });

        // ASSERT
        assertThat(response.getHeader(RequestIdFilter.HEADER)).isEqualTo("req-123.abc");
    }

    @Test
    @DisplayName("NFR-SEC-006: replaces an incoming id that could forge log lines")
    void unsafeHeader_isReplaced() throws Exception {
        // ARRANGE
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER, "abc\r\nFAKE LOG LINE");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // ACT
        filter.doFilter(request, response, (req, res) -> { });

        // ASSERT
        assertThat(response.getHeader(RequestIdFilter.HEADER))
                .doesNotContain("\r", "\n", "FAKE")
                .matches("[0-9a-f-]{36}");
    }

    @Test
    @DisplayName("NFR-SEC-006: clears the MDC after the request, even when the chain fails")
    void mdcIsClearedAfterRequest() {
        // ARRANGE
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain failing = (req, res) -> { throw new IllegalStateException("boom"); };

        // ACT
        try {
            filter.doFilter(request, response, failing);
        } catch (Exception expected) {
            // se espera
        }

        // ASSERT
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}