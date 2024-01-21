package com.example.pokerv2.service;

import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.HudRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HudRepository hudRepository;
    private final BCryptPasswordEncoder encoder;

    public User join(User user){
        Optional<User> findUser = userRepository.findByUserId(user.getUserId());
        if(findUser.isPresent())
            throw new CustomException(ErrorCode.DUPLICATE_USER);

        User savedUser = userRepository.save(user);

        Hud hud = new Hud();
        hud.setUser(user);
        Hud saveHud = hudRepository.save(hud);
        savedUser.setHud(saveHud);
        savedUser.setMoney(10000000);
        savedUser.setPassword(encoder.encode(savedUser.getPassword()));
        savedUser.setRoles(Collections.singletonList("ROLE_USER"));
        userRepository.save(savedUser);
        return savedUser;
    } // User를 리턴할 필요가 있나요?
}
