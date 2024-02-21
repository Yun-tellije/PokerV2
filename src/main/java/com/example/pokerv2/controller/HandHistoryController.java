package com.example.pokerv2.controller;

import com.example.pokerv2.dto.HandHistoryDto;
import com.example.pokerv2.service.HandHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RequestMapping("/api/handHistory")
@RestController
@RequiredArgsConstructor
public class HandHistoryController {

    private final HandHistoryService handHistoryService;

    @GetMapping
    public List<HandHistoryDto> get(Principal principal) {
        System.out.println("handhistory");
        return handHistoryService.get(principal.getName());
    }
}