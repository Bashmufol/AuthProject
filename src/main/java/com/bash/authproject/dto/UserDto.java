package com.bash.authproject.dto;

import com.bash.authproject.model.User;

public record UserDto(String username, String firstName, String lastName, String email) {
    public UserDto (User user){
        this(user.getUsername(), user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
