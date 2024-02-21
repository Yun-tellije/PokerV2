package com.example.pokerv2.stomp;

import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WebSocketAuthenticatorService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    public UsernamePasswordAuthenticationToken getAuthenticatedOrFail(final String  username, final String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Username was null or empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationCredentialsNotFoundException("Password was null or empty.");
        }
        Optional<User> findUser = userRepository.findByUserId(username);
        if (findUser.isEmpty()) {
            throw new BadCredentialsException("Bad credentials for user " + username);
        }


        User user = findUser.get();
        if(!encoder.matches(password, user.getPassword()))
            throw new BadCredentialsException("Bad credentials for user " + username);


        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                user.getAuthorities()
        );
    }
}