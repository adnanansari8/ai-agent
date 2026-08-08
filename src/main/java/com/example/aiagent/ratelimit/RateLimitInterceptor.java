package com.example.aiagent.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String userId = getCurrentUserId();
        String tier = (String) request.getAttribute("userTier");
        if (tier == null) tier = "default";

        if (!rateLimitService.isAllowed(userId, tier)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("""
                {"error": "Rate limit exceeded", 
                 "retryAfter": 60, 
                 "tier": "%s"}""".formatted(tier));
            return false;
        }

        // Add rate limit headers
        RateLimitService.RateLimitStatus status =
                rateLimitService.getStatus(userId, tier);
        response.setHeader("X-RateLimit-Limit", String.valueOf(status.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(status.remaining()));

        return true;
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails user) {
            return user.getUsername();
        }
        return "anonymous";
    }
}