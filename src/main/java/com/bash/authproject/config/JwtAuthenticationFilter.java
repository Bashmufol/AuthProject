package com.bash.authproject.config;

import com.bash.authproject.service.CustomUserDetailsService;
import com.bash.authproject.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
}
