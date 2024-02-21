package com.example.pokerv2.controller;

import com.example.pokerv2.dto.HudDto;
import com.example.pokerv2.service.HudService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hud")
public class HudController {

    private final HudService hudService;

    @GetMapping("/{userId}")
    public HudDto get(@PathVariable Long userId) {
        return hudService.get(userId);
    }

    @GetMapping
    public HudDto get(Principal principal) {
        return hudService.get(principal.getName());
    }
}