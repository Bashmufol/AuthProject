package com.bash.authproject.service;

import com.bash.authproject.dto.*;
import com.bash.authproject.exceptions.UserNotFoundException;
import com.bash.authproject.model.PasswordResetToken;
import com.bash.authproject.model.RefreshToken;
import com.bash.authproject.model.ResponseModel;
import com.bash.authproject.model.User;
import com.bash.authproject.repository.PasswordResetTokenRepo;
import com.bash.authproject.repository.RefreshTokenRepo;
import com.bash.authproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final PasswordResetTokenRepo passwordResetTokenRepo;


    public ResponseModel<UserDto> registerUser(RegisterDto request){
        HttpStatus status = HttpStatus.CREATED;
        User newUser = new User();
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setIsActive(true);
        userRepository.save(newUser);
        return new ResponseModel<>(status.value(), "Registration Successful", new UserDto(newUser));
    }

    @Transactional
    public ResponseModel<AuthResponseDto> loginUser(LoginDto request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        HttpStatus status = HttpStatus.OK;
        User user = findUserByUsername(request.username());
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String AccessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        Date expiryDate = jwtService.extractExpiration(refreshToken);
        refreshTokenRepo.deleteByUserId(user.getId());

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setToken(refreshToken);
        newRefreshToken.setExpiryDate(expiryDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()); // Convert java.util.Date to LocalDateTime
        newRefreshToken.setUser(user);
        refreshTokenRepo.save(newRefreshToken);
        AuthResponseDto authresponse = new AuthResponseDto(AccessToken, refreshToken);
        return new ResponseModel<>(status.value(), "Login Successful", authresponse);
    }


    @Transactional
    public ResponseModel<UserDto> updateCurrentUserProfile(UpdateUserDto request){
        HttpStatus status = HttpStatus.OK;
        String username = getCurrentAuthenticatedUsername();
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        return new ResponseModel<>(status.value(), "Profile updated successfully", new UserDto(user));
    }

    @Transactional
    public void deactivateCurrentUserProfile(){
        String username = getCurrentAuthenticatedUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User " + username + " not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }


//    Retrieves the username of the currently authenticated user from the SecurityContext
//    and returns the username
    private String getCurrentAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getUsername();
    }

    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("User with username " + username + " not found"));
    }

    @Transactional
    public void initiatePasswordReset(ForgotPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User with email " + request.email() + " not found"));

        String token = UUID.randomUUID().toString(); // Generate a unique token

        // Set token to expire in 10 minutes
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(10);

        passwordResetTokenRepo.deleteByUserId(user.getId());

        PasswordResetToken newPasswordResetToken = new PasswordResetToken();
        newPasswordResetToken.setToken(token);
        newPasswordResetToken.setExpiryDate(expiryDate);
        newPasswordResetToken.setUser(user);
        passwordResetTokenRepo.save(newPasswordResetToken);

        try {
            // Call the EmailService to send the email
            emailService.sendPasswordResetEmail(user.getEmail(), token, user.getUsername());
        } catch (MailException e) {
            // Log the error for internal monitoring
            System.err.println("Failed to send password reset email to " + user.getEmail() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email. Please try again later.", e);
        }
    }

    @Transactional
    public void resetPassword(ResetPasswordDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User with email " + request.email() + " not found"));

        PasswordResetToken passwordResetToken = passwordResetTokenRepo.findByToken(request.token())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired password reset token supplied"));

        // Check if the token has expired
        if (passwordResetToken.getExpiryDate() == null || passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepo.delete(passwordResetToken);
            throw new IllegalStateException("Password reset token has expired.");
        }

        if(!passwordResetToken.getUser().equals(user)) {
            throw new IllegalStateException("Token does not match the provided email");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        passwordResetTokenRepo.delete(passwordResetToken);
    }

    @Transactional
    public AuthResponseDto getNewAccessToken(RefreshTokenRequestDto request) {

        String providedRefreshToken = request.refreshToken();

        //  Find the RefreshToken entity by the provided refresh token
        RefreshToken existingRefreshtoken = refreshTokenRepo.findByToken(providedRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired Refresh token. Please log in again."));

        //  Check if the supplied token has expired
        if (existingRefreshtoken.getExpiryDate() == null || existingRefreshtoken.getExpiryDate().isBefore(LocalDateTime.now())) {

            // Invalidate the old refresh token if it's expired
            refreshTokenRepo.delete(existingRefreshtoken);
            throw new IllegalArgumentException("Invalid or expired Refresh token. Please log in again.");
        }
        //     Get the user from the token entity
        User user = existingRefreshtoken.getUser();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String newAccessToken = jwtService.generateAccessToken(userPrincipal);

        return new AuthResponseDto(newAccessToken, providedRefreshToken);

    }

}
