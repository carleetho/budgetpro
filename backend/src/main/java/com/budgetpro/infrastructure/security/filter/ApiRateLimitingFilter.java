package com.budgetpro.infrastructure.security.filter;

import com.budgetpro.infrastructure.rest.error.ErrorResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

/**
 * Rate limiting HTTP de tres tiers (REQ-47 Task 8) con Resilience4j in-memory.
 * <ul>
 *   <li>{@code auth-login}: login/register — 5 / 15min por IP</li>
 *   <li>{@code api-public}: {@code /api/public/**} — 100 / h por IP</li>
 *   <li>{@code api-per-user}: resto de {@code /api/**} — 1000 / h por usuario (o IP si anónimo)</li>
 * </ul>
 * Preferido frente a {@code @RateLimiter} en cada controller para no explotar blast radius
 * y permitir buckets por clave (usuario/IP).
 */
@Component
public class ApiRateLimitingFilter extends OncePerRequestFilter {

    public static final String HEADER_LIMIT = "X-RateLimit-Limit";
    public static final String HEADER_REMAINING = "X-RateLimit-Remaining";
    public static final String HEADER_RESET = "X-RateLimit-Reset";

    private static final String CONFIG_AUTH = "auth-login";
    private static final String CONFIG_PUBLIC = "api-public";
    private static final String CONFIG_USER = "api-per-user";

    private final RateLimiterRegistry rateLimiterRegistry;
    private final ObjectMapper objectMapper;
    private final boolean enabled;

    public ApiRateLimitingFilter(RateLimiterRegistry rateLimiterRegistry,
                                 ObjectMapper objectMapper,
                                 @Value("${app.rate-limit.enabled:true}") boolean enabled) {
        this.rateLimiterRegistry = rateLimiterRegistry;
        this.objectMapper = objectMapper;
        this.enabled = enabled;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = path(request);
        if (!path.startsWith("/api/")) {
            return true;
        }
        // health-style under api unlikely; still skip docs if ever mounted under /api
        return path.startsWith("/api/v1/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = path(request);
        Tier tier = resolveTier(path);
        String key = resolveKey(request, tier);
        String limiterName = tier.configName() + ":" + key;

        RateLimiterConfig config = rateLimiterRegistry.getConfiguration(tier.configName())
                .orElseGet(rateLimiterRegistry::getDefaultConfig);
        RateLimiter limiter = rateLimiterRegistry.rateLimiter(limiterName, config);

        try {
            RateLimiter.waitForPermission(limiter);
        } catch (RequestNotPermitted ex) {
            writeTooManyRequests(response, config);
            return;
        }

        attachHeaders(response, limiter, config);
        filterChain.doFilter(request, response);
    }

    private static Tier resolveTier(String path) {
        if (path.equals("/api/v1/auth/login") || path.equals("/api/v1/auth/register")) {
            return Tier.AUTH;
        }
        if (path.startsWith("/api/public/")) {
            return Tier.PUBLIC;
        }
        return Tier.USER;
    }

    private static String resolveKey(HttpServletRequest request, Tier tier) {
        if (tier == Tier.USER) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && auth.getName() != null
                    && !"anonymousUser".equals(auth.getName())) {
                return "user:" + auth.getName();
            }
        }
        return "ip:" + clientIp(request);
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return Optional.ofNullable(request.getRemoteAddr()).orElse("unknown");
    }

    private static String path(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri;
    }

    private static void attachHeaders(HttpServletResponse response, RateLimiter limiter, RateLimiterConfig config) {
        int limit = config.getLimitForPeriod();
        int remaining = Math.max(0, limiter.getMetrics().getAvailablePermissions());
        long resetEpochSec = (System.currentTimeMillis() + config.getLimitRefreshPeriod().toMillis()) / 1000L;
        response.setHeader(HEADER_LIMIT, String.valueOf(limit));
        response.setHeader(HEADER_REMAINING, String.valueOf(remaining));
        response.setHeader(HEADER_RESET, String.valueOf(resetEpochSec));
    }

    private void writeTooManyRequests(HttpServletResponse response, RateLimiterConfig config) throws IOException {
        Duration refresh = config.getLimitRefreshPeriod();
        long retryAfterSec = Math.max(1L, refresh.toSeconds());
        long resetEpochSec = (System.currentTimeMillis() + refresh.toMillis()) / 1000L;

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(retryAfterSec));
        response.setHeader(HEADER_LIMIT, String.valueOf(config.getLimitForPeriod()));
        response.setHeader(HEADER_REMAINING, "0");
        response.setHeader(HEADER_RESET, String.valueOf(resetEpochSec));

        objectMapper.writeValue(response.getOutputStream(),
                ErrorResponses.error(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        "RATE_LIMIT_EXCEEDED",
                        "Rate limit exceeded. Try again later."));
    }

    private enum Tier {
        AUTH(CONFIG_AUTH),
        PUBLIC(CONFIG_PUBLIC),
        USER(CONFIG_USER);

        private final String configName;

        Tier(String configName) {
            this.configName = configName;
        }

        String configName() {
            return configName;
        }
    }
}
