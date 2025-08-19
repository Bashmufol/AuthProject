package com.bash.authproject.dto;

public record ResetPasswordDto(String token, String newPassword) {
}
