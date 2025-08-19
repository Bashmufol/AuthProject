package com.bash.authproject.dto;

//Dto to request for new access token from the refresh token
public record RefreshTokenRequestDto(String refreshToken) {}
