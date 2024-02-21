package com.example.pokerv2.controller;

import com.example.pokerv2.dto.UserDto;
import com.example.pokerv2.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public UserDto getUserProfile(Principal principal){
        return userService.getMyProfile(principal);
    }

    @GetMapping(value = "/image/{userId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable Long userId) {

        byte[] img = userService.getUserImage(userId);
        return new ResponseEntity<>(img, HttpStatus.OK);
    }

    @PostMapping("/image")
    public UserDto updateImage(@RequestParam MultipartFile file, Principal principal){
        return userService.updateUserImage(file, principal);
    }
}
