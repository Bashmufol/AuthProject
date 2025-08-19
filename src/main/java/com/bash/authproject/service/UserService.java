package com.bash.authproject.service;

import com.bash.authproject.dto.ForgotPasswordRequestDto;
import com.bash.authproject.dto.LoginDto;
import com.bash.authproject.dto.RegisterDto;
import com.bash.authproject.dto.UpdateUserDto;
import com.bash.authproject.model.User;
import com.bash.authproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public void registerUser(RegisterDto request){
        User newUser = new User();
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setIsActive(true);
        userRepository.save(newUser);
    }

    public String loginUser(LoginDto request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        User user = findUserByUsername(request.username());
        UserPrincipal userPrincipal = new UserPrincipal(user);
        return jwtService.generateToken(userPrincipal);
    }

    public User updateCurrentUserProfile(UpdateUserDto request){
        String username = getCurrentAuthenticatedUsername();
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        return userRepository.save(user);
    }

    public void deactivateCurrentUserProfile(){
        String username = getCurrentAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }


    private String getCurrentAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUsername();
    }

    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
    }

    public void initiatePasswordReset(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.email()) // You need findByEmail in UserRepository
                .orElseThrow(() -> new EntityNotFoundException("User with email " + request.email() + " not found"));

        String token = UUID.randomUUID().toString(); // Generate a unique token
        // Set token to expire in 1 hour (adjust as needed)
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1);

        user.setResetPasswordToken(token);
        user.setResetTokenExpiryDate(expiryDate);
        userRepository.save(user);
        System.out.println("Password reset token for " + user.getEmail() + ": " + token);
        // Example: emailService.sendPasswordResetEmail(user.getEmail(), token);
    }



}
