package com.bash.authproject.dto;

//Dto for when returning of access and refresh token
public record AuthResponseDto(String accessToken, String refreshToken) {
}
