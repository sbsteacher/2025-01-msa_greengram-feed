package com.green.greengram.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.green.greengram.configuration.model.SignedUser;
import com.green.greengram.configuration.model.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String signedUserId = request.getHeader("signedUser");
        log.info("signedUserId: {}", signedUserId);

        if (signedUserId != null) {
            SignedUser signedUser = new SignedUser(Long.parseLong(signedUserId));
            Authentication auth = new UsernamePasswordAuthenticationToken(signedUser, null, null);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
