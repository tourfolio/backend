package com.tourfolio.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_PREFIX = "TOKEN_";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(TOKEN_PREFIX)) {
            try {
                String token = authHeader.substring(TOKEN_PREFIX.length());
                Long userId = extractUserIdFromToken(token);

                if (userId != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authentication set for userId: {}", userId);
                }
            } catch (Exception e) {
                log.warn("Failed to parse token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

    private Long extractUserIdFromToken(String token) {
        // Token format: {uuid}_{userId}
        String[] parts = token.split("_");
        if (parts.length >= 2) {
            try {
                return Long.parseLong(parts[parts.length - 1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid userId in token: {}", parts[parts.length - 1]);
            }
        }
        return null;
    }
}
