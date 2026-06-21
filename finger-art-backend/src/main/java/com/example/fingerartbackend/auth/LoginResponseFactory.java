package com.example.fingerartbackend.auth;

import com.example.fingerartbackend.dto.LoginResponse;
import com.example.fingerartbackend.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoginResponseFactory {

    @Autowired
    private JwtTokenService jwtTokenService;

    public LoginResponse build(User user) {
        LoginResponse response = new LoginResponse();
        response.setToken(jwtTokenService.generateToken(user));
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenService.getExpirationMs(user));
        response.setUser(user);
        response.setRequiresTotp(false);
        return response;
    }

    public LoginResponse buildRequiresTotp(User user) {
        LoginResponse response = new LoginResponse();
        response.setPreAuthToken(jwtTokenService.generatePreAuthToken(user));
        response.setRequiresTotp(true);
        response.setUser(user);
        return response;
    }
}
