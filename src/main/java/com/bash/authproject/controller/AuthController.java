package com.bash.authproject.controller;

import com.bash.authproject.dto.*;
import com.bash.authproject.model.User;
import com.bash.authproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;


    //    Handles user registration
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto request) {
        userService.registerUser(request);
        return ResponseEntity.ok("Registration successful");
    }

//    Handles user login
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto request){
        AuthResponseDto authResponse = userService.loginUser(request);
        return ResponseEntity.ok(authResponse);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<User> updateProfile(@RequestBody UpdateUserDto request){
        User updatedUser = userService.updateCurrentUserProfile(request);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/deactivate-profile")
    public ResponseEntity<String> deactivateProfile(){
        userService.deactivateCurrentUserProfile();
        return ResponseEntity.ok("Your account is Deactivated");
    }

    // Endpoint to initiate forgot password process
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        userService.initiatePasswordReset(request);
        // Important: Always return a generic success message to prevent user enumeration
        return ResponseEntity.ok("a password reset link has been sent to: " + request.email());
    }

    // Endpoint to reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDto request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password has been successfully reset.");
    }

    // Endpoint for refreshing access token using refresh token
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDto> refreshToken(@RequestBody RefreshTokenRequestDto request){
        AuthResponseDto authResponse = userService.refreshAccessToken(request);
        return ResponseEntity.ok(authResponse);
    }
}
