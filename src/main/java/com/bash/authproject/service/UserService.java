package com.bash.authproject.service;

import com.bash.authproject.dto.*;
import com.bash.authproject.model.User;
import com.bash.authproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
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
    private final EmailService emailService;

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

//        if (user == null) {
//            System.out.println("Password reset requested for non-existent email: " + request.email());
//            return; // Exit silently if user not found, but client still gets success message.
//        }

        String token = UUID.randomUUID().toString(); // Generate a unique token
        // Set token to expire in 1 hour (adjust as needed)
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        user.setResetPasswordToken(token);
        user.setResetTokenExpiryDate(expiryDate);
        userRepository.save(user);

        try {
            // NEW: Call the EmailService to send the email
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
        } catch (MailException e) {
            // Log the error for internal monitoring, but don't expose sensitive details to the user.
            System.err.println("Failed to send password reset email to " + user.getEmail() + ": " + e.getMessage());
            // You might want to throw a custom, user-friendly exception here, or rethrow as a RuntimeException
            // if this failure should indicate a problem to the client.
            throw new RuntimeException("Failed to send password reset email. Please try again later.", e);
        }
    }

    public void resetPassword(ResetPasswordDto request) {
        User user = userRepository.findByResetPasswordToken(request.token()) // You need findByResetPasswordToken in UserRepository
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired password reset token."));

        // Check if the token has expired
        if (user.getResetTokenExpiryDate() == null || user.getResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Password reset token has expired.");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetPasswordToken(null);        // Clear token after use
        user.setResetTokenExpiryDate(null); // Clear expiry date
        userRepository.save(user);
    }

}
