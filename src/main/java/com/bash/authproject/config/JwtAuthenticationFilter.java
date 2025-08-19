package com.bash.authproject.config;

import com.bash.authproject.service.CustomUserDetailsService;
import com.bash.authproject.service.JwtService;
import com.bash.authproject.service.UserPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Extract the Authorization header from the incoming request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Check if the Authorization header is missing or does not start with "Bearer "
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // Exit the filter since no JWT is present or valid
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        // Check if a username was successfully extracted from the JWT
        // and if the current user is not already authenticated in the SecurityContext
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details from the database using the extracted username
            UserPrincipal userPrincipal = (UserPrincipal) this.customUserDetailsService.loadUserByUsername(username);
            if(jwtService.isTokenValid(jwt, userPrincipal)) {
                UsernamePasswordAuthenticationToken authToken= new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities());

                // Set additional details about the authentication request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Authenticate the user by setting the authentication token in the SecurityContext
                // This makes the user authenticated for the duration of the request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
