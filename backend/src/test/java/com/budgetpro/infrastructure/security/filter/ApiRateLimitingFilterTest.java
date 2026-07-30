package com.budgetpro.infrastructure.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ApiRateLimitingFilterTest {

    private RateLimiterRegistry registry;
    private ApiRateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        RateLimiterConfig authConfig = RateLimiterConfig.custom()
                .limitForPeriod(2)
                .limitRefreshPeriod(Duration.ofMinutes(15))
                .timeoutDuration(Duration.ZERO)
                .build();
        registry = RateLimiterRegistry.of(RateLimiterConfig.ofDefaults());
        registry.addConfiguration("auth-login", authConfig);
        registry.addConfiguration("api-public", RateLimiterConfig.custom()
                .limitForPeriod(100)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ZERO)
                .build());
        registry.addConfiguration("api-per-user", RateLimiterConfig.custom()
                .limitForPeriod(1000)
                .limitRefreshPeriod(Duration.ofHours(1))
                .timeoutDuration(Duration.ZERO)
                .build());
        filter = new ApiRateLimitingFilter(registry, new ObjectMapper().findAndRegisterModules(), true);
    }

    @Test
    void login_returns429_afterLimitExceeded() throws ServletException, IOException {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            req.setRemoteAddr("10.0.0.1");
            MockHttpServletResponse res = new MockHttpServletResponse();
            filter.doFilter(req, res, chain);
            assertThat(res.getStatus()).isNotEqualTo(429);
        }

        MockHttpServletRequest blocked = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        blocked.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(blocked, res, chain);

        assertThat(res.getStatus()).isEqualTo(429);
        assertThat(res.getHeader("Retry-After")).isNotBlank();
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_LIMIT)).isEqualTo("2");
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_REMAINING)).isEqualTo("0");
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_RESET)).isNotBlank();
        assertThat(res.getContentAsString()).contains("RATE_LIMIT_EXCEEDED");
    }

    @Test
    void successfulRequest_setsRateLimitHeaders() throws ServletException, IOException {
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/presupuestos");
        req.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse res = new MockHttpServletResponse();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_LIMIT)).isEqualTo("1000");
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_REMAINING)).isNotBlank();
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_RESET)).isNotBlank();
    }

    @Test
    void disabled_skipsLimiting() throws ServletException, IOException {
        ApiRateLimitingFilter disabled = new ApiRateLimitingFilter(registry, new ObjectMapper().findAndRegisterModules(), false);
        FilterChain chain = mock(FilterChain.class);
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse res = new MockHttpServletResponse();

        disabled.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        assertThat(res.getHeader(ApiRateLimitingFilter.HEADER_LIMIT)).isNull();
    }
}
