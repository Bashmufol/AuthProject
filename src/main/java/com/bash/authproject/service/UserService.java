package com.bash.authproject.service;

import com.bash.authproject.dto.RegisterDto;
import com.bash.authproject.dto.UpdateUserDto;
import com.bash.authproject.model.User;
import com.bash.authproject.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public User findUserByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("User with username " + username + " not found"));
    }

    public User updateUser(String username, UpdateUserDto request){
        User user = userRepository.findByUsername(username).
                orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        return userRepository.save(user);
    }

    public void deactivateUser(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
        user.setIsActive(false);
        userRepository.save(user);
    }

}
