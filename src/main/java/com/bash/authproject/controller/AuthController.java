package com.bash.authproject.controller;

import com.bash.authproject.dto.*;
import com.bash.authproject.model.User;
import com.bash.authproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth/**")
public class AuthController {
    private final AuthenticationManager authManager;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    //    Handles user registration
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Registration successful");
    }

//    Handles user login
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password)
        );
        User user = userService.findByUsername(request.username());
        String jwtToken = jwtService.generateToken(user);
        return ResponseEntity.ok(jwtToken);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateUserDto request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        User updatedUser = userService.updateUser(currentUser.getUsername, request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/deactivate-profile")
    public ResponseEntity<String> deactivateProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        userService.deactivateUser(currentUser.getUsername());
        return ResponseEntity.ok("Your account is Deactivated");
    }
}
