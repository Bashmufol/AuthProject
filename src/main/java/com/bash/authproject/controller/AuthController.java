package com.bash.authproject.controller;

import com.bash.authproject.dto.*;
import com.bash.authproject.model.ResponseModel;
import com.bash.authproject.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    //    TODO: handle global exception

    //    Handles user registration
    @PostMapping("/register")
    public ResponseModel<UserDto> register(@RequestBody RegisterDto request) {
        return userService.registerUser(request);
//        TODO: return basic user detail with no password or any other sensitive data
    }

//    Handles user login
    @PostMapping("/login")
    public ResponseModel<AuthResponseDto> login(@RequestBody LoginDto request){
        return userService.loginUser(request);
    }
    // Endpoint to update profile
    @PutMapping("/update-profile")
    public ResponseModel<UserDto> updateProfile(@RequestBody UpdateUserDto request){
        return userService.updateCurrentUserProfile(request);
    }
    // Endpoint to deactivate profile
    @PostMapping("/deactivate-profile")
    public ResponseEntity<String> deactivateProfile(){
        userService.deactivateCurrentUserProfile();
        return ResponseEntity.ok("Your account is Deactivated");
    }

    // Endpoint to initiate forgot password process
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDto request) {
        userService.initiatePasswordReset(request);
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
