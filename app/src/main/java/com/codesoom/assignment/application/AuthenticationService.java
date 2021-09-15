package com.codesoom.assignment.application;

import com.codesoom.assignment.domain.User;
import com.codesoom.assignment.domain.UserRepository;
import com.codesoom.assignment.dto.UserLoginData;
import com.codesoom.assignment.errors.EmailNotFoundException;
import com.codesoom.assignment.errors.UnauthorizedException;
import com.codesoom.assignment.errors.WrongPasswordException;
import com.codesoom.assignment.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

/**
 * 유저 인증 로직 담당.
 */
@Service
public class AuthenticationService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public AuthenticationService(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }


    public Long parsetoken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            throw new UnauthorizedException(accessToken);
        }
        try {
            Claims claims = jwtUtil.decode(accessToken);
            return claims.get("userId", Long.class);
        } catch (SignatureException e) {
            throw new UnauthorizedException(accessToken);
        }
    }

    public String createToken(UserLoginData loginData) {
        User user = findUserByEmail(loginData);
        if (!user.authenticate(loginData.getPassword())) {
            throw new WrongPasswordException(loginData);
        }
        return jwtUtil.encode(user.getId());
    }

    private User findUserByEmail(UserLoginData loginData) {
        return userRepository.findByEmail(loginData.getEmail())
                .orElseThrow(() -> new EmailNotFoundException(loginData));
    }


}

