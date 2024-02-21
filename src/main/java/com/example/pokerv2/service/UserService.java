package com.example.pokerv2.service;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.error.CustomException;
import com.example.pokerv2.error.ErrorCode;
import com.example.pokerv2.model.Hud;
import com.example.pokerv2.model.User;
import com.example.pokerv2.repository.HudRepository;
import com.example.pokerv2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final HudRepository hudRepository;
    private final BCryptPasswordEncoder encoder;
    @Value("${file:}")
    private String rootFilePath;

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
    }

    public UserDto getMyProfile(Principal principal){
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        return new UserDto(user);
    }

    public byte[] getUserImage(Long userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        try{
            Path path = Paths.get(rootFilePath, user.getImagePath()).toAbsolutePath();
            Resource imageRescource = new UrlResource(path.toUri());

            if(imageRescource.exists()){
                return Files.readAllBytes(path);
            } else{
                return new byte[0];
            }
        } catch (IOException e){
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    public UserDto updateUserImage(MultipartFile file, Principal principal){
        User user = userRepository.findByUserId(principal.getName()).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        try{
            Path path = Paths.get(rootFilePath, user.getImagePath()).toAbsolutePath();
            if(user.getImagePath() != null){
                Files.deleteIfExists(path);
            }

            Path newPath = Paths.get(rootFilePath, user.getImagePath()).toAbsolutePath();
            Files.createDirectories(newPath.getParent());
            file.transferTo(newPath.toFile());

        } catch(IOException e){
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return new UserDto(user);
    }
}
