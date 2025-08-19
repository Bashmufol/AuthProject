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
    public ResponseEntity<String> login(@RequestBody LoginDto request){
        String jwtToken = userService.loginUser(request);
        return ResponseEntity.ok(jwtToken);
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
}
