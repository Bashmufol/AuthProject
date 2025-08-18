package com.bash.authproject.repository;

import com.bash.authproject.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository {
    public User findByUsername(String username);
}
