package com.bash.authproject.repository;

import com.bash.authproject.exceptions.UserNotFoundException;
import com.bash.authproject.model.User;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setFirstName("Bashir");
        user1.setLastName("Muhammed");
        user1.setUsername("user1");
        user1.setIsActive(true);

        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setFirstName("Bashir");
        user2.setLastName("Muhammed");
        user2.setUsername("user2");
        user2.setIsActive(true);

        userRepository.save(user2);
    }

    @Test
    void findByEmail() {
//        When
        User u = userRepository.findByEmail("user1@example.com")
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        assertThat(u.getEmail()).isEqualTo("user1@example.com");
    }
    @Test
    void existsByEmail() {
//        Given
        String email = "user@example.com";
//        When
        boolean expected = userRepository.existsByEmail(email);
//      Then
        assertThat(expected).isTrue();
    }
}